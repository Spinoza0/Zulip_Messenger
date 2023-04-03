package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.*
import com.spinoza.messenger_tfs.data.model.event.*
import com.spinoza.messenger_tfs.data.model.message.*
import com.spinoza.messenger_tfs.data.model.presence.AllPresencesResponse
import com.spinoza.messenger_tfs.data.model.stream.StreamDto
import com.spinoza.messenger_tfs.data.model.user.AllUsersResponse
import com.spinoza.messenger_tfs.data.model.user.OwnUserResponse
import com.spinoza.messenger_tfs.data.model.user.UserDto
import com.spinoza.messenger_tfs.data.model.user.UserResponse
import com.spinoza.messenger_tfs.data.network.ZulipApiFactory
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.model.event.*
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

    override suspend fun getOwnUser(): RepositoryResult<User> {
        return getUserFromApi(null)
    }

    override suspend fun getUser(userId: Long): RepositoryResult<User> {
        return getUserFromApi(userId)
    }

    override suspend fun getUsersByFilter(usersFilter: String): RepositoryResult<List<User>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.getAllUsers()
                when (response.isSuccessful) {
                    true ->
                        handleGetUsersByFilterResult(response.getBodyOrThrow(), usersFilter)
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
    ): RepositoryResult<MessagesResult> = withContext(Dispatchers.IO) {
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
                        RESULT_SUCCESS -> {
                            val messageDto = MessageDto(
                                sendMessageResponse.messageId,
                                messagesFilter.channel.channelId,
                                ownUser.userId,
                                content,
                                "",
                                UNDEFINED_ID,
                                System.currentTimeMillis() / MILLIS_IN_SECOND,
                                messagesFilter.topic.name,
                                false,
                                emptyList(),
                                ownUser.fullName,
                                ownUser.email,
                                ownUser.avatarUrl
                            )
                            messagesCache.add(messageDto)
                            RepositoryResult.Success(
                                MessagesResult(
                                    messagesCache.getMessages(messagesFilter)
                                        .toDomain(ownUser.userId),
                                    MessagePosition(
                                        MessagePosition.Type.LAST_POSITION,
                                        sendMessageResponse.messageId
                                    )
                                )
                            )
                        }
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

    override suspend fun registerEventQueue(
        eventTypes: List<EventType>,
    ): RepositoryResult<EventsQueue> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.registerEventQueue(
                    eventTypes = Json.encodeToString(eventTypes.toStringsList())
                )
                when (response.isSuccessful) {
                    true -> {
                        val registerResponse = response.getBodyOrThrow()
                        when (registerResponse.result) {
                            RESULT_SUCCESS -> RepositoryResult.Success(
                                EventsQueue(registerResponse.queueId, registerResponse.lastEventId)
                            )
                            else -> RepositoryResult.Failure.RegisterPresenceEventQueue(
                                registerResponse.msg
                            )
                        }
                    }
                    false -> RepositoryResult.Failure.RegisterPresenceEventQueue(response.message())
                }
            }.getOrElse {
                RepositoryResult.Failure.Network(getErrorText(it))
            }
        }

    override suspend fun deleteEventQueue(queueId: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.deleteEventQueue(queueId)
            }
        }
    }

    override suspend fun getPresenceEvents(
        queue: EventsQueue,
    ): RepositoryResult<List<PresenceEvent>> {
        return getEvents(queue, EventType.PRESENCE)
    }

    override suspend fun getChannelEvents(queue: EventsQueue): RepositoryResult<List<ChannelEvent>> {
        return getEvents(queue, EventType.CHANNEL)
    }

    override suspend fun getMessageEvents(
        queue: EventsQueue,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<MessageEvent> {
        val deleteMessageEventsResult: RepositoryResult<List<DeleteMessageEventDto>> =
            getEvents(queue, EventType.DELETE_MESSAGE)
        val reactionEventsResult: RepositoryResult<List<ReactionEventDto>> =
            getEvents(queue, EventType.REACTION)
        val messageEventsResult: RepositoryResult<List<MessageEventDto>> =
            getEvents(queue, EventType.MESSAGE)
        var lastEventId = UNDEFINED_EVENT_ID
        if (deleteMessageEventsResult is RepositoryResult.Success) {
            deleteMessageEventsResult.value.forEach { deleteMessageEventDto ->
                messagesCache.remove(deleteMessageEventDto.messageId)
                lastEventId = maxOf(lastEventId, deleteMessageEventDto.id)
            }
        }
        if (reactionEventsResult is RepositoryResult.Success) {
            reactionEventsResult.value.forEach { reactionEventDto ->
                messagesCache.updateReaction(reactionEventDto)
                lastEventId = maxOf(lastEventId, reactionEventDto.id)
            }
        }
        if (messageEventsResult is RepositoryResult.Success) {
            messageEventsResult.value.forEach { messageEventDto ->
                messagesCache.add(messageEventDto.message)
                lastEventId = messageEventDto.id
            }
        }
        return if (lastEventId != UNDEFINED_EVENT_ID) {
            RepositoryResult.Success(
                MessageEvent(
                    lastEventId,
                    MessagesResult(
                        messagesCache.getMessages(messagesFilter).toDomain(ownUser.userId),
                        MessagePosition()
                    )
                )
            )
        } else {
            RepositoryResult.Failure.GetEvents("")
        }
    }

    private suspend fun getUserFromApi(userId: Long?): RepositoryResult<User> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response =
                    if (userId == null) apiService.getOwnUser() else apiService.getUser(userId)
                when (response.isSuccessful) {
                    true -> {
                        var userDto: UserDto? = null
                        val responseMsg: String
                        if (userId == null) {
                            val ownUserResponse = response.body() as OwnUserResponse
                            responseMsg = ownUserResponse.msg
                            if (ownUserResponse.result == RESULT_SUCCESS) {
                                isOwnUserLoaded = true
                                ownUser = ownUserResponse.toUserDto()
                                userDto = ownUser
                            }
                        } else {
                            val userResponse = response.body() as UserResponse
                            responseMsg = userResponse.msg
                            if (userResponse.result == RESULT_SUCCESS) {
                                userDto = userResponse.user
                            }
                        }

                        if (userDto != null) {
                            val presence = getUserPresence(userDto.userId)
                            RepositoryResult.Success(userDto.toDomain(presence))
                        } else if (userId == null) {
                            RepositoryResult.Failure.OwnUserNotFound(responseMsg)
                        } else {
                            RepositoryResult.Failure.UserNotFound(userId, responseMsg)
                        }
                    }
                    false -> if (userId == null) {
                        RepositoryResult.Failure.OwnUserNotFound(response.message())
                    } else {
                        RepositoryResult.Failure.UserNotFound(userId, response.message())
                    }
                }
            }.getOrElse {
                RepositoryResult.Failure.Network(getErrorText(it))
            }
        }

    private suspend fun handleGetUsersByFilterResult(
        allUsersResponse: AllUsersResponse,
        usersFilter: String,
    ) = when (allUsersResponse.result) {
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

    private suspend inline fun <reified R> getEvents(
        queue: EventsQueue,
        eventType: EventType,
    ): RepositoryResult<R> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getEventsFromQueue(queue.queueId, queue.lastEventId)
            when (response.isSuccessful) {
                true -> {
                    val eventResponseBody = response.getBodyOrThrow()
                    when (eventType) {
                        EventType.PRESENCE -> {
                            val eventResponse = jsonConverter.decodeFromString(
                                PresenceEventsResponse.serializer(),
                                eventResponseBody.string()
                            )
                            when (eventResponse.result) {
                                RESULT_SUCCESS -> RepositoryResult.Success(
                                    eventResponse.events.toDomain() as R
                                )
                                else -> RepositoryResult.Failure.GetEvents(eventResponse.msg)
                            }
                        }
                        EventType.CHANNEL -> {
                            val eventResponse = jsonConverter.decodeFromString(
                                StreamEventsResponse.serializer(),
                                eventResponseBody.string()
                            )
                            when (eventResponse.result) {
                                RESULT_SUCCESS -> RepositoryResult.Success(
                                    eventResponse.events.listToDomain() as R
                                )
                                else -> RepositoryResult.Failure.GetEvents(eventResponse.msg)
                            }
                        }
                        EventType.DELETE_MESSAGE -> {
                            val eventResponse = jsonConverter.decodeFromString(
                                DeleteMessageEventsResponse.serializer(),
                                eventResponseBody.string()
                            )
                            when (eventResponse.result) {
                                RESULT_SUCCESS ->
                                    RepositoryResult.Success(eventResponse.events as R)
                                else -> RepositoryResult.Failure.GetEvents(eventResponse.msg)
                            }
                        }
                        EventType.REACTION -> {
                            val eventResponse = jsonConverter.decodeFromString(
                                ReactionEventsResponse.serializer(),
                                eventResponseBody.string()
                            )
                            when (eventResponse.result) {
                                RESULT_SUCCESS ->
                                    RepositoryResult.Success(eventResponse.events as R)
                                else -> RepositoryResult.Failure.GetEvents(eventResponse.msg)
                            }
                        }
                        EventType.MESSAGE -> {
                            val eventResponse = jsonConverter.decodeFromString(
                                MessageEventsResponse.serializer(),
                                eventResponseBody.string()
                            )
                            when (eventResponse.result) {
                                RESULT_SUCCESS ->
                                    RepositoryResult.Success(eventResponse.events as R)
                                else -> RepositoryResult.Failure.GetEvents(eventResponse.msg)
                            }
                        }
                    }
                }
                false -> RepositoryResult.Failure.GetEvents(response.message())
            }
        }.getOrElse {
            RepositoryResult.Failure.Network(getErrorText(it))
        }
    }

    private suspend fun updateReaction(
        reactions: List<ReactionDto>,
        messageId: Long,
        emoji: Emoji,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<MessagesResult> = runCatching {
        val isAddReaction = null == reactions.find {
            it.emojiName == emoji.name && it.userId == ownUser.userId
        }
        val response = if (isAddReaction) {
            apiService.addReaction(messageId, emoji.name)
        } else {
            apiService.removeReaction(messageId, emoji.name)
        }
        when (response.isSuccessful) {
            true -> {
                val updateReactionResponse = response.getBodyOrThrow()
                when (updateReactionResponse.result) {
                    RESULT_SUCCESS -> {
                        messagesCache.updateReaction(
                            messageId,
                            emoji.toDto(ownUser.userId),
                            isAddReaction
                        )
                        RepositoryResult.Success(
                            MessagesResult(
                                messagesCache.getMessages(messagesFilter).toDomain(ownUser.userId),
                                MessagePosition(MessagePosition.Type.EXACTLY, messageId)
                            )
                        )
                    }
                    else -> RepositoryResult.Failure.UpdatingReaction(updateReactionResponse.msg)
                }
            }
            false -> RepositoryResult.Failure.UpdatingReaction(response.message())
        }
    }.getOrElse {
        RepositoryResult.Failure.UpdatingReaction(getErrorText(it))
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
            users.filter { it.fullName.contains(usersFilter, true) }
        }
        RepositoryResult.Success(userList)
    } else {
        RepositoryResult.Failure.LoadingUsers(usersResponse.msg)
    }

    private inline fun <reified T> Response<T>?.getBodyOrThrow(): T {
        return this?.body() ?: throw RuntimeException("Empty response body")
    }

    private fun MessagesFilter.createNarrow(): String {
        val narrowDtoList = mutableListOf(
            NarrowItemDto(NarrowOperator.STREAM.value, channel.name)
        )
        if (topic.name.isNotEmpty()) {
            narrowDtoList.add(NarrowItemDto(NarrowOperator.TOPIC.value, topic.name))
        }
        return Json.encodeToString(narrowDtoList)
    }

    private fun getErrorText(e: Throwable): String =
        e.localizedMessage ?: e.message ?: e.toString()

    companion object {

        private const val RESULT_SUCCESS = "success"
        private const val UNDEFINED_EVENT_ID = -1L
        private const val UNDEFINED_ID = -1
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