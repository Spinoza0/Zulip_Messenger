package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.network.ZulipApiFactory
import com.spinoza.messenger_tfs.data.network.model.event.*
import com.spinoza.messenger_tfs.data.network.model.message.ReactionDto
import com.spinoza.messenger_tfs.data.network.model.presence.AllPresencesResponse
import com.spinoza.messenger_tfs.data.network.model.stream.StreamDto
import com.spinoza.messenger_tfs.data.network.model.user.AllUsersResponse
import com.spinoza.messenger_tfs.data.network.model.user.UserDto
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.model.event.*
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


// TODO: 1) отрефакторить - не все сообщения сразу отправлять, а только новые или измененные
// TODO: 2) пагинация для сообщений

class MessagesRepositoryImpl private constructor() : MessagesRepository {

    private val messagesCache = MessagesCache()
    private var ownUser: UserDto = UserDto()
    private var isOwnUserLoaded = false
    private val apiService = ZulipApiFactory.apiService
    private val jsonConverter = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun setOwnStatusActive() {
        withContext(Dispatchers.IO) {
            runCatchingNonCancellation { apiService.setOwnStatusActive() }
        }
    }

    override suspend fun getOwnUserId(): RepositoryResult<Long> {
        return if (isOwnUserLoaded) {
            RepositoryResult.Success(ownUser.userId)
        } else
            when (val result = getOwnUser()) {
                is RepositoryResult.Success -> RepositoryResult.Success(ownUser.userId)
                is RepositoryResult.Failure.OwnUserNotFound ->
                    RepositoryResult.Failure.OwnUserNotFound(result.value)
                is RepositoryResult.Failure.Network -> RepositoryResult.Failure.Network(result.value)
                else -> RepositoryResult.Failure.Network("")
            }
    }

    override suspend fun getOwnUser(): RepositoryResult<User> = withContext(Dispatchers.IO) {
        runCatchingGetRepositoryResult {
            val response = apiService.getOwnUser()
            when (response.isSuccessful) {
                true -> {
                    val ownUserResponse = response.getBodyOrThrow()
                    if (ownUserResponse.result == RESULT_SUCCESS) {
                        isOwnUserLoaded = true
                        ownUser = ownUserResponse.toUserDto()
                        val presence = getUserPresence(ownUser.userId)
                        RepositoryResult.Success(ownUser.toDomain(presence))
                    } else {
                        RepositoryResult.Failure.OwnUserNotFound(ownUserResponse.msg)
                    }
                }
                false -> RepositoryResult.Failure.OwnUserNotFound(response.message())
            }
        }
    }

    override suspend fun getUser(userId: Long): RepositoryResult<User> =
        withContext(Dispatchers.IO) {
            runCatchingGetRepositoryResult {
                val response = apiService.getUser(userId)
                when (response.isSuccessful) {
                    true -> {
                        val userResponse = response.getBodyOrThrow()
                        if (userResponse.result == RESULT_SUCCESS) {
                            val presence = getUserPresence(userResponse.user.userId)
                            RepositoryResult.Success(userResponse.user.toDomain(presence))
                        } else {
                            RepositoryResult.Failure.UserNotFound(userId, userResponse.msg)
                        }
                    }
                    false -> RepositoryResult.Failure.UserNotFound(userId, response.message())
                }
            }
        }

    override suspend fun getUsersByFilter(usersFilter: String): RepositoryResult<List<User>> =
        withContext(Dispatchers.IO) {
            runCatchingGetRepositoryResult {
                val response = apiService.getAllUsers()
                when (response.isSuccessful) {
                    true -> {
                        val allUsersResponse = response.getBodyOrThrow()
                        when (allUsersResponse.result) {
                            RESULT_SUCCESS -> {
                                val presencesResponse = apiService.getAllPresences()
                                when (presencesResponse.isSuccessful) {
                                    true -> makeAllUsersAnswer(
                                        usersFilter,
                                        allUsersResponse,
                                        presencesResponse.body()
                                    )
                                    false -> makeAllUsersAnswer(usersFilter, allUsersResponse)
                                }
                            }
                            else -> RepositoryResult.Failure.LoadingUsers(allUsersResponse.msg)
                        }
                    }
                    false -> RepositoryResult.Failure.LoadingUsers(response.message())
                }
            }
        }

    override suspend fun getMessages(
        filter: MessagesFilter,
    ): RepositoryResult<MessagesResult> = withContext(Dispatchers.IO) {
        runCatchingGetRepositoryResult {
            if (ownUser.userId == UserDto.UNDEFINED_ID) {
                getOwnUser()
            }
            val response =
                apiService.getMessages(narrow = filter.createNarrowJsonWithOperator())
            when (response.isSuccessful) {
                true -> {
                    val messagesResponse = response.getBodyOrThrow()
                    when (messagesResponse.result) {
                        RESULT_SUCCESS -> {
                            messagesCache.addAll(messagesResponse.messages)
                            RepositoryResult.Success(
                                MessagesResult(
                                    messagesCache.getMessages(filter).toDomain(ownUser.userId),
                                    MessagePosition()
                                )
                            )
                        }
                        else -> RepositoryResult.Failure.LoadingMessages(
                            filter,
                            messagesResponse.msg
                        )
                    }
                }
                false ->
                    RepositoryResult.Failure.LoadingMessages(filter, response.message())
            }
        }
    }

    override suspend fun getChannels(
        channelsFilter: ChannelsFilter,
    ): RepositoryResult<List<Channel>> = withContext(Dispatchers.IO) {
        runCatchingGetRepositoryResult {
            var streamsList: List<StreamDto> = emptyList()
            var errorMsg = ""
            if (channelsFilter.isSubscribed) {
                val response = apiService.getSubscribedStreams()
                if (response.isSuccessful) {
                    val subscribedStreamsDto = response.getBodyOrThrow()
                    if (subscribedStreamsDto.result == RESULT_SUCCESS) {
                        streamsList = subscribedStreamsDto.subscriptions
                    } else {
                        errorMsg = subscribedStreamsDto.msg
                    }
                } else {
                    errorMsg = response.message()
                }
            } else {
                val response = apiService.getAllStreams()
                if (response.isSuccessful) {
                    val allStreamsDto = response.getBodyOrThrow()
                    if (allStreamsDto.result == RESULT_SUCCESS) {
                        streamsList = allStreamsDto.streams
                    } else {
                        errorMsg = allStreamsDto.msg
                    }
                } else {
                    errorMsg = response.message()
                }
            }
            if (streamsList.isNotEmpty() && errorMsg.isEmpty()) {
                RepositoryResult.Success(streamsList.toDomain(channelsFilter))
            } else {
                RepositoryResult.Failure.LoadingChannels(channelsFilter, errorMsg)
            }
        }
    }

    override suspend fun getTopics(channel: Channel): RepositoryResult<List<Topic>> =
        withContext(Dispatchers.IO) {
            runCatchingGetRepositoryResult {
                val response = apiService.getTopics(channel.channelId)
                when (response.isSuccessful) {
                    true -> {
                        val topicsResponseDto = response.getBodyOrThrow()
                        when (topicsResponseDto.result) {
                            RESULT_SUCCESS -> {
                                val filter = MessagesFilter(channel)
                                updateMessagesCache(filter)
                                RepositoryResult.Success(
                                    topicsResponseDto.topics.toDomain(
                                        messagesCache.getMessages(filter)
                                    )
                                )
                            }
                            else -> RepositoryResult.Failure.LoadingChannelTopics(
                                channel,
                                topicsResponseDto.msg
                            )
                        }
                    }
                    false -> RepositoryResult.Failure.LoadingChannelTopics(
                        channel,
                        response.message()
                    )
                }
            }
        }

    override suspend fun getTopic(
        filter: MessagesFilter,
    ): RepositoryResult<Topic> = withContext(Dispatchers.IO) {
        updateMessagesCache(filter)
        RepositoryResult.Success(
            Topic(filter.topic.name, messagesCache.getMessages(filter).size)
        )
    }

    override suspend fun sendMessage(
        content: String,
        filter: MessagesFilter,
    ): RepositoryResult<Long> = withContext(Dispatchers.IO) {
        runCatchingGetRepositoryResult {
            val response = apiService.sendMessageToStream(
                filter.channel.channelId,
                filter.topic.name,
                content
            )
            when (response.isSuccessful) {
                true -> {
                    val sendMessageResponse = response.getBodyOrThrow()
                    when (sendMessageResponse.result) {
                        RESULT_SUCCESS -> RepositoryResult.Success(sendMessageResponse.messageId)
                        else -> RepositoryResult.Failure.SendingMessage(sendMessageResponse.msg)
                    }
                }
                false -> RepositoryResult.Failure.SendingMessage(response.message())
            }
        }
    }

    override suspend fun updateReaction(
        messageId: Long,
        emoji: Emoji,
        filter: MessagesFilter,
    ): RepositoryResult<MessagesResult> = withContext(Dispatchers.IO) {
        runCatchingGetRepositoryResult {
            val response = apiService.getSingleMessage(messageId)
            when (response.isSuccessful) {
                true -> {
                    val singleMessageResponse = response.getBodyOrThrow()
                    when (singleMessageResponse.result) {
                        RESULT_SUCCESS -> updateReaction(
                            singleMessageResponse.message.reactions,
                            messageId,
                            emoji,
                            filter
                        )
                        else -> RepositoryResult.Failure.UpdatingReaction(singleMessageResponse.msg)
                    }
                }
                false -> RepositoryResult.Failure.UpdatingReaction(response.message())
            }
        }
    }

    private suspend fun updateMessagesCache(filter: MessagesFilter) = runCatchingNonCancellation {
        val response =
            apiService.getMessages(narrow = filter.createNarrowJsonWithOperator())
        if (response.isSuccessful) {
            messagesCache.addAll(response.getBodyOrThrow().messages)
        }
    }

    override suspend fun registerEventQueue(
        eventTypes: List<EventType>,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<EventsQueue> = withContext(Dispatchers.IO) {
        runCatchingGetRepositoryResult {
            val response = apiService.registerEventQueue(
                narrow = messagesFilter.createNarrowJsonForEvents(),
                eventTypes = Json.encodeToString(eventTypes.toStringsList())
            )
            when (response.isSuccessful) {
                true -> {
                    val registerResponse = response.getBodyOrThrow()
                    when (registerResponse.result) {
                        RESULT_SUCCESS -> RepositoryResult.Success(
                            EventsQueue(registerResponse.queueId, registerResponse.lastEventId)
                        )
                        else -> RepositoryResult.Failure.RegisterEventQueue(
                            registerResponse.msg
                        )
                    }
                }
                false -> RepositoryResult.Failure.RegisterEventQueue(response.message())
            }
        }
    }

    override suspend fun deleteEventQueue(queueId: String): Unit = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            apiService.deleteEventQueue(queueId)
        }
    }

    override suspend fun getPresenceEvents(
        queue: EventsQueue,
    ): RepositoryResult<List<PresenceEvent>> = withContext(Dispatchers.IO) {
        runCatchingGetRepositoryResult {
            val response = apiService.getEventsFromQueue(queue.queueId, queue.lastEventId)
            when (response.isSuccessful) {
                true -> {
                    val eventResponseBody = response.getBodyOrThrow()
                    val eventResponse = jsonConverter.decodeFromString(
                        PresenceEventsResponse.serializer(), eventResponseBody.string()
                    )
                    when (eventResponse.result) {
                        RESULT_SUCCESS -> RepositoryResult.Success(
                            eventResponse.events.toDomain()
                        )
                        else -> RepositoryResult.Failure.GetEvents(eventResponse.msg)
                    }
                }
                false -> RepositoryResult.Failure.GetEvents(response.message())
            }
        }
    }

    override suspend fun getChannelEvents(
        queue: EventsQueue,
    ): RepositoryResult<List<ChannelEvent>> = withContext(Dispatchers.IO) {
        runCatchingGetRepositoryResult {
            val response = apiService.getEventsFromQueue(queue.queueId, queue.lastEventId)
            when (response.isSuccessful) {
                true -> {
                    val eventResponseBody = response.getBodyOrThrow()
                    val eventResponse = jsonConverter.decodeFromString(
                        StreamEventsResponse.serializer(), eventResponseBody.string()
                    )
                    when (eventResponse.result) {
                        RESULT_SUCCESS -> RepositoryResult.Success(
                            eventResponse.events.listToDomain()
                        )
                        else -> RepositoryResult.Failure.GetEvents(eventResponse.msg)
                    }

                }
                false -> RepositoryResult.Failure.GetEvents(response.message())
            }
        }
    }

    override suspend fun getMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
    ): RepositoryResult<MessageEvent> = withContext(Dispatchers.IO) {
        runCatchingGetRepositoryResult {
            val response =
                apiService.getEventsFromQueue(queue.queueId, queue.lastEventId)
            when (response.isSuccessful) {
                true -> {
                    val eventResponse = jsonConverter.decodeFromString(
                        MessageEventsResponse.serializer(), response.getBodyOrThrow().string()
                    )
                    when (eventResponse.result) {
                        RESULT_SUCCESS -> {
                            eventResponse.events.forEach { messageEventDto ->
                                messagesCache.add(messageEventDto.message)
                            }
                            RepositoryResult.Success(
                                MessageEvent(
                                    eventResponse.events.last().id,
                                    MessagesResult(
                                        messagesCache.getMessages(filter).toDomain(ownUser.userId),
                                        MessagePosition()
                                    )
                                )
                            )
                        }
                        else -> RepositoryResult.Failure.GetEvents(eventResponse.msg)
                    }
                }
                false -> RepositoryResult.Failure.GetEvents(response.message())
            }
        }
    }

    override suspend fun getDeleteMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
    ): RepositoryResult<DeleteMessageEvent> = withContext(Dispatchers.IO) {
        runCatchingGetRepositoryResult {
            val response =
                apiService.getEventsFromQueue(queue.queueId, queue.lastEventId)
            when (response.isSuccessful) {
                true -> {
                    val eventResponse = jsonConverter.decodeFromString(
                        DeleteMessageEventsResponse.serializer(), response.getBodyOrThrow().string()
                    )
                    when (eventResponse.result) {
                        RESULT_SUCCESS -> {
                            eventResponse.events.forEach { deleteMessageEventDto ->
                                messagesCache.remove(deleteMessageEventDto.messageId)
                            }
                            RepositoryResult.Success(
                                DeleteMessageEvent(
                                    eventResponse.events.last().id,
                                    MessagesResult(
                                        messagesCache.getMessages(filter).toDomain(ownUser.userId),
                                        MessagePosition()
                                    )
                                )
                            )
                        }
                        else -> RepositoryResult.Failure.GetEvents(eventResponse.msg)
                    }
                }
                false -> RepositoryResult.Failure.GetEvents(response.message())
            }
        }
    }

    override suspend fun getReactionEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
    ): RepositoryResult<ReactionEvent> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            val response =
                apiService.getEventsFromQueue(queue.queueId, queue.lastEventId)
            when (response.isSuccessful) {
                true -> {
                    val eventResponse = jsonConverter.decodeFromString(
                        ReactionEventsResponse.serializer(), response.getBodyOrThrow().string()
                    )
                    when (eventResponse.result) {
                        RESULT_SUCCESS -> {
                            eventResponse.events.forEach { reactionEventDto ->
                                messagesCache.updateReaction(reactionEventDto)
                            }
                            RepositoryResult.Success(
                                ReactionEvent(
                                    eventResponse.events.last().id,
                                    MessagesResult(
                                        messagesCache.getMessages(filter).toDomain(ownUser.userId),
                                        MessagePosition()
                                    )
                                )
                            )
                        }
                        else -> RepositoryResult.Failure.GetEvents(eventResponse.msg)
                    }
                }
                false -> RepositoryResult.Failure.GetEvents(response.message())
            }
        }.getOrElse {
            RepositoryResult.Failure.GetEvents(it.getErrorText())
        }
    }

    private suspend fun updateReaction(
        reactions: List<ReactionDto>,
        messageId: Long,
        emoji: Emoji,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<MessagesResult> {
        val isAddReaction = null == reactions.find {
            it.emojiName == emoji.name && it.userId == ownUser.userId
        }
        messagesCache.updateReaction(messageId, emoji.toDto(ownUser.userId), isAddReaction)
        CoroutineScope(Dispatchers.IO).launch {
            runCatchingNonCancellation {
                if (isAddReaction) {
                    apiService.addReaction(messageId, emoji.name)
                } else {
                    apiService.removeReaction(messageId, emoji.name)
                }
            }
        }
        // Using cached messages -> Success
        return RepositoryResult.Success(
            MessagesResult(
                messagesCache.getMessages(messagesFilter).toDomain(ownUser.userId),
                MessagePosition(MessagePosition.Type.EXACTLY, messageId)
            )
        )
    }

    private suspend fun getUserPresence(userId: Long): User.Presence = runCatchingNonCancellation {
        val response = apiService.getUserPresence(userId)
        when (response.isSuccessful) {
            true -> {
                val presenceResponse = response.getBodyOrThrow()
                when (presenceResponse.result) {
                    RESULT_SUCCESS -> presenceResponse.presence.toDomain()
                    else -> User.Presence.OFFLINE
                }
            }
            false -> User.Presence.OFFLINE
        }
    }.getOrElse {
        User.Presence.OFFLINE
    }

    private fun makeAllUsersAnswer(
        usersFilter: String,
        usersResponse: AllUsersResponse,
        presencesResponse: AllPresencesResponse? = null,
    ): RepositoryResult<List<User>> = if (usersResponse.result == RESULT_SUCCESS) {
        val users = mutableListOf<User>()
        val timestamp = System.currentTimeMillis() / MILLIS_IN_SECOND
        usersResponse.members
            .filter { it.isBot.not() && it.isActive }
            .forEach { userDto ->
                val presence =
                    if (presencesResponse != null && presencesResponse.result == RESULT_SUCCESS) {
                        presencesResponse.presences[userDto.email]?.let { presenceDto ->
                            if (timestamp - presenceDto.aggregated.timestamp < OFFLINE_TIME) {
                                presenceDto.toDomain()
                            } else {
                                User.Presence.OFFLINE
                            }
                        } ?: User.Presence.OFFLINE
                    } else {
                        User.Presence.OFFLINE
                    }
                users.add(userDto.toDomain(presence))
            }
        val userList = if (usersFilter.isBlank()) {
            users
        } else {
            users.filter {
                it.fullName.contains(usersFilter, true) ||
                        it.email.contains(usersFilter, true)
            }
        }
        RepositoryResult.Success(userList)
    } else {
        RepositoryResult.Failure.LoadingUsers(usersResponse.msg)
    }

    companion object {

        private const val RESULT_SUCCESS = "success"
        private const val MILLIS_IN_SECOND = 1000
        private const val OFFLINE_TIME = 180

        @Volatile
        private var instance: MessagesRepositoryImpl? = null
        private val LOCK = Unit

        fun getInstance(): MessagesRepositoryImpl {
            instance?.let { return it }
            synchronized(LOCK) {
                instance?.let { return it }
                return MessagesRepositoryImpl().also { instance = it }
            }
        }
    }
}