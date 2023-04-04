package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.*
import com.spinoza.messenger_tfs.data.model.event.*
import com.spinoza.messenger_tfs.data.model.message.MessagesResponse
import com.spinoza.messenger_tfs.data.model.message.NarrowItemDto
import com.spinoza.messenger_tfs.data.model.message.NarrowOperator
import com.spinoza.messenger_tfs.data.model.message.ReactionDto
import com.spinoza.messenger_tfs.data.model.presence.AllPresencesResponse
import com.spinoza.messenger_tfs.data.model.stream.StreamDto
import com.spinoza.messenger_tfs.data.model.user.AllUsersResponse
import com.spinoza.messenger_tfs.data.model.user.UserDto
import com.spinoza.messenger_tfs.data.network.ZulipApiFactory
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
import okhttp3.ResponseBody
import retrofit2.Response


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
            runCatching { apiService.setOwnStatusActive() }
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
        runCatching {
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
        }.getOrElse {
            RepositoryResult.Failure.Network(getErrorText(it))
        }
    }

    override suspend fun getUser(userId: Long): RepositoryResult<User> =
        withContext(Dispatchers.IO) {
            runCatching {
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
            }.getOrElse {
                RepositoryResult.Failure.Network(getErrorText(it))
            }
        }

    override suspend fun getUsersByFilter(usersFilter: String): RepositoryResult<List<User>> =
        withContext(Dispatchers.IO) {
            runCatching {
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
            }.getOrElse {
                RepositoryResult.Failure.Network(getErrorText(it))
            }
        }

    override suspend fun getMessages(
        messagesFilter: MessagesFilter,
        messageId: Long,
    ): RepositoryResult<MessagesResult> = withContext(Dispatchers.IO) {
        runCatching {
            val response =
                apiService.getMessages(narrow = messagesFilter.createNarrow())
            when (response.isSuccessful) {
                true ->
                    handleGetMessagesResult(response.getBodyOrThrow(), messageId, messagesFilter)
                false ->
                    RepositoryResult.Failure.LoadingMessages(messagesFilter, response.message())
            }
        }.getOrElse {
            RepositoryResult.Failure.Network(getErrorText(it))
        }
    }

    override suspend fun getChannels(
        channelsFilter: ChannelsFilter,
    ): RepositoryResult<List<Channel>> = withContext(Dispatchers.IO) {
        runCatching {
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
        }.getOrElse {
            RepositoryResult.Failure.Network(getErrorText(it))
        }
    }

    override suspend fun getTopics(channel: Channel): RepositoryResult<List<Topic>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.getTopics(channel.channelId)
                when (response.isSuccessful) {
                    true -> {
                        val topicsResponseDto = response.getBodyOrThrow()
                        when (topicsResponseDto.result) {
                            RESULT_SUCCESS -> {
                                val messagesFilter = MessagesFilter(channel)
                                updateMessagesCache(messagesFilter)
                                RepositoryResult.Success(
                                    topicsResponseDto.topics.toDomain(
                                        messagesCache.getMessages(messagesFilter)
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
            }.getOrElse {
                RepositoryResult.Failure.Network(getErrorText(it))
            }
        }

    override suspend fun getTopic(
        messagesFilter: MessagesFilter,
    ): RepositoryResult<Topic> = withContext(Dispatchers.IO) {
        updateMessagesCache(messagesFilter)
        RepositoryResult.Success(
            Topic(messagesFilter.topic.name, messagesCache.getMessages(messagesFilter).size)
        )
    }

    override suspend fun sendMessage(
        content: String,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<Long> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.sendMessageToStream(
                messagesFilter.channel.channelId,
                messagesFilter.topic.name,
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
        }.getOrElse {
            RepositoryResult.Failure.Network(getErrorText(it))
        }
    }

    override suspend fun updateReaction(
        messageId: Long,
        emoji: Emoji,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<MessagesResult> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getSingleMessage(messageId)
            when (response.isSuccessful) {
                true -> {
                    val singleMessageResponse = response.getBodyOrThrow()
                    when (singleMessageResponse.result) {
                        RESULT_SUCCESS -> updateReaction(
                            singleMessageResponse.message.reactions,
                            messageId,
                            emoji,
                            messagesFilter
                        )
                        else -> RepositoryResult.Failure.UpdatingReaction(singleMessageResponse.msg)
                    }
                }
                false -> RepositoryResult.Failure.UpdatingReaction(response.message())
            }
        }.getOrElse {
            RepositoryResult.Failure.UpdatingReaction(getErrorText(it))
        }
    }

    private suspend fun updateMessagesCache(messagesFilter: MessagesFilter) {
        runCatching {
            val response =
                apiService.getMessages(narrow = messagesFilter.createNarrow())
            if (response.isSuccessful) {
                messagesCache.replaceAll(response.getBodyOrThrow().messages)
            }
        }
    }

    private fun handleGetMessagesResult(
        messagesResponse: MessagesResponse,
        messageId: Long,
        messagesFilter: MessagesFilter,
    ) = when (messagesResponse.result) {
        RESULT_SUCCESS -> {
            val positionType = if (messageId != Message.UNDEFINED_ID) {
                if (messagesResponse.messages.last().id == messageId) {
                    MessagePosition.Type.LAST_POSITION
                } else {
                    MessagePosition.Type.EXACTLY
                }
            } else {
                MessagePosition.Type.UNDEFINED
            }
            messagesCache.replaceAll(messagesResponse.messages)
            RepositoryResult.Success(
                MessagesResult(
                    messagesResponse.messages.toDomain(ownUser.userId),
                    MessagePosition(positionType, messageId)
                )
            )
        }
        else -> RepositoryResult.Failure.LoadingMessages(
            messagesFilter,
            messagesResponse.msg
        )
    }

    override suspend fun registerEventQueue(
        eventTypes: List<EventType>,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<EventsQueue> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.registerEventQueue(
                narrow = messagesFilter.createNarrow(),
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
        }.getOrElse {
            RepositoryResult.Failure.Network(getErrorText(it))
        }
    }

    override suspend fun deleteEventQueue(queueId: String): Unit = withContext(Dispatchers.IO) {
        runCatching {
            apiService.deleteEventQueue(queueId)
        }
    }

    override suspend fun getPresenceEvents(
        queue: EventsQueue,
    ): RepositoryResult<List<PresenceEvent>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getEventsFromQueue(queue.queueId, queue.lastEventId)
            when (response.isSuccessful) {
                true -> {
                    val eventResponseBody = response.getBodyOrThrow()
                    val eventResponse = jsonConverter.decodeFromString(
                        PresenceEventsResponse.serializer(),
                        eventResponseBody.string()
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
        }.getOrElse {
            RepositoryResult.Failure.Network(getErrorText(it))
        }
    }

    override suspend fun getChannelEvents(
        queue: EventsQueue,
    ): RepositoryResult<List<ChannelEvent>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getEventsFromQueue(queue.queueId, queue.lastEventId)
            when (response.isSuccessful) {
                true -> {
                    val eventResponseBody = response.getBodyOrThrow()
                    val eventResponse = jsonConverter.decodeFromString(
                        StreamEventsResponse.serializer(),
                        eventResponseBody.string()
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
        }.getOrElse {
            RepositoryResult.Failure.Network(getErrorText(it))
        }
    }

    override suspend fun getMessageEvents(
        queue: EventsQueue,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<MessagesEvent> = withContext(Dispatchers.IO) {
        runCatching {
            val response =
                apiService.getEventsFromQueue(queue.queueId, queue.lastEventId)
            when (response.isSuccessful) {
                true -> {
                    val eventBody = response.getBodyOrThrow()
                    val deleteMessageEventsResult =
                        getMessagesEvents(eventBody, EventType.DELETE_MESSAGE, messagesFilter)
                    if (deleteMessageEventsResult is RepositoryResult.Success) {
                        deleteMessageEventsResult
                    } else {
                        val reactionEventsResult =
                            getMessagesEvents(eventBody, EventType.REACTION, messagesFilter)
                        if (reactionEventsResult is RepositoryResult.Success) {
                            reactionEventsResult
                        } else {
                            getMessagesEvents(eventBody, EventType.MESSAGE, messagesFilter)
                        }
                    }
                }
                false -> RepositoryResult.Failure.GetEvents(response.message())
            }
        }.getOrElse {
            RepositoryResult.Failure.GetEvents(getErrorText(it))
        }
    }

    private suspend fun getMessagesEvents(
        eventBody: ResponseBody,
        eventType: EventType,
        messagesFilter: MessagesFilter = MessagesFilter(),
    ): RepositoryResult<MessagesEvent> = withContext(Dispatchers.IO) {
        runCatching {
            val eventString = eventBody.string()
            when (eventType) {
                EventType.DELETE_MESSAGE -> {
                    val eventResponse = jsonConverter.decodeFromString(
                        DeleteMessageEventsResponse.serializer(), eventString
                    )
                    when (eventResponse.result) {
                        RESULT_SUCCESS -> {
                            val lastEventId = eventResponse.events.last().id
                            eventResponse.events.forEach { deleteMessageEventDto ->
                                messagesCache.remove(deleteMessageEventDto.messageId)
                            }
                            RepositoryResult.Success(
                                MessagesEvent(
                                    lastEventId, MessagesResult(
                                        messagesCache.getMessages(messagesFilter)
                                            .toDomain(ownUser.userId),
                                        MessagePosition()
                                    )
                                )
                            )
                        }
                        else -> RepositoryResult.Failure.GetEvents(eventResponse.msg)
                    }
                }
                EventType.REACTION -> {
                    val eventResponse = jsonConverter.decodeFromString(
                        ReactionEventsResponse.serializer(), eventString
                    )
                    when (eventResponse.result) {
                        RESULT_SUCCESS -> {
                            val lastEventId = eventResponse.events.last().id
                            eventResponse.events.forEach { reactionEventDto ->
                                messagesCache.updateReaction(reactionEventDto)
                            }
                            RepositoryResult.Success(
                                MessagesEvent(
                                    lastEventId, MessagesResult(
                                        messagesCache.getMessages(messagesFilter)
                                            .toDomain(ownUser.userId),
                                        MessagePosition()
                                    )
                                )
                            )
                        }
                        else -> RepositoryResult.Failure.GetEvents(eventResponse.msg)
                    }
                }
                EventType.MESSAGE -> {
                    val eventResponse = jsonConverter.decodeFromString(
                        MessageEventsResponse.serializer(), eventString
                    )
                    when (eventResponse.result) {
                        RESULT_SUCCESS -> {
                            val lastEventId = eventResponse.events.last().id
                            eventResponse.events.forEach { messageEventDto ->
                                messagesCache.add(messageEventDto.message)
                            }
                            RepositoryResult.Success(
                                MessagesEvent(
                                    lastEventId,
                                    MessagesResult(
                                        messagesCache.getMessages(messagesFilter)
                                            .toDomain(ownUser.userId),
                                        MessagePosition()
                                    )
                                )
                            )
                        }
                        else -> RepositoryResult.Failure.GetEvents(eventResponse.msg)
                    }
                }
                else -> throw RuntimeException("Invalid EventType: $eventType")
            }
        }.getOrElse {
            RepositoryResult.Failure.GetEvents(getErrorText(it))
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
            runCatching {
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

    private suspend fun getUserPresence(userId: Long): User.Presence = runCatching {
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

    private inline fun <reified T> Response<T>?.getBodyOrThrow(): T {
        return this?.body() ?: throw RuntimeException("Empty response body")
    }

    private fun MessagesFilter.createNarrow(): String {
        val narrowDtoList = mutableListOf<NarrowItemDto>()
        if (channel.name.isNotEmpty()) {
            narrowDtoList.add(NarrowItemDto(NarrowOperator.STREAM.value, channel.name))
        }
        if (topic.name.isNotEmpty()) {
            narrowDtoList.add(NarrowItemDto(NarrowOperator.TOPIC.value, topic.name))
        }
        return Json.encodeToString(narrowDtoList)
    }

    private fun getErrorText(e: Throwable): String =
        e.localizedMessage ?: e.message ?: e.toString()

    companion object {

        private const val RESULT_SUCCESS = "success"
        private const val MILLIS_IN_SECOND = 1000
        private const val OFFLINE_TIME = 180

        private var instance: MessagesRepositoryImpl? = null
        private val LOCK = Unit

        fun getInstance(): MessagesRepositoryImpl {
            instance?.let {
                return it
            }
            synchronized(LOCK) {
                instance?.let {
                    return it
                }
                val newInstance = MessagesRepositoryImpl()
                instance = newInstance
                return newInstance
            }
        }
    }
}