package com.spinoza.messenger_tfs.data.repository

import com.cyberfox21.tinkofffintechseminar.di.GlobalDI
import com.spinoza.messenger_tfs.data.network.ZulipApiService
import com.spinoza.messenger_tfs.data.network.model.event.*
import com.spinoza.messenger_tfs.data.network.model.message.ReactionDto
import com.spinoza.messenger_tfs.data.network.model.presence.AllPresencesResponse
import com.spinoza.messenger_tfs.data.network.model.stream.StreamDto
import com.spinoza.messenger_tfs.data.network.model.user.AllUsersResponse
import com.spinoza.messenger_tfs.data.network.model.user.UserDto
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.model.event.*
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryError
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
    private val apiService = GlobalDI.INSTANCE.apiService
    private val jsonConverter = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun setOwnStatusActive() {
        withContext(Dispatchers.IO) {
            runCatchingNonCancellation { apiService.setOwnStatusActive() }
        }
    }

    override suspend fun getOwnUserId(): Result<Long> {
        return if (isOwnUserLoaded) {
            Result.success(ownUser.userId)
        } else {
            val result = getOwnUser()
            if (result.isSuccess) {
                Result.success(ownUser.userId)
            } else {
                Result.failure(result.exceptionOrNull() ?: RepositoryError(""))
            }
        }
    }

    override suspend fun getOwnUser(): Result<User> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            val response = apiService.getOwnUser()
            if (!response.isSuccessful) {
                throw RepositoryError(response.message())
            }
            val ownUserResponse = response.getBodyOrThrow()
            if (ownUserResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(ownUserResponse.msg)
            }
            isOwnUserLoaded = true
            ownUser = ownUserResponse.toUserDto()
            val presence = getUserPresence(ownUser.userId)
            ownUser.toDomain(presence)
        }
    }

    override suspend fun getUser(userId: Long): Result<User> =
        withContext(Dispatchers.IO) {
            runCatchingNonCancellation {
                val response = apiService.getUser(userId)
                if (!response.isSuccessful) {
                    throw RepositoryError(response.message())
                }
                val userResponse = response.getBodyOrThrow()
                if (userResponse.result != RESULT_SUCCESS) {
                    throw RepositoryError(userResponse.msg)
                }
                val presence = getUserPresence(userResponse.user.userId)
                userResponse.user.toDomain(presence)
            }
        }

    override suspend fun getUsersByFilter(usersFilter: String): Result<List<User>> =
        withContext(Dispatchers.IO) {
            runCatchingNonCancellation {
                val response = apiService.getAllUsers()
                if (!response.isSuccessful) {
                    throw RepositoryError(response.message())
                }
                val allUsersResponse = response.getBodyOrThrow()
                if (allUsersResponse.result != RESULT_SUCCESS) {
                    throw RepositoryError(allUsersResponse.msg)
                }
                val presencesResponse = apiService.getAllPresences()
                if (presencesResponse.isSuccessful) {
                    makeAllUsersAnswer(usersFilter, allUsersResponse, presencesResponse.body())
                } else {
                    makeAllUsersAnswer(usersFilter, allUsersResponse)
                }
            }
        }

    override suspend fun getMessages(
        filter: MessagesFilter,
    ): Result<MessagesResult> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            if (ownUser.userId == UserDto.UNDEFINED_ID) {
                getOwnUser()
            }
            val response = apiService.getMessages(
                anchor = ZulipApiService.ANCHOR_FIRST_UNREAD,
                narrow = filter.createNarrowJsonWithOperator()
            )
            if (!response.isSuccessful) {
                throw RepositoryError(response.message())
            }
            val messagesResponse = response.getBodyOrThrow()
            if (messagesResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(messagesResponse.msg)
            }
            val position = if (messagesResponse.foundAnchor) {
                MessagePosition(MessagePosition.Type.EXACTLY, messagesResponse.anchor)
            } else {
                MessagePosition(MessagePosition.Type.LAST_POSITION)
            }
            messagesCache.addAll(messagesResponse.messages)
            MessagesResult(messagesCache.getMessages(filter).toDomain(ownUser.userId), position)
        }
    }

    override suspend fun getChannels(
        channelsFilter: ChannelsFilter,
    ): Result<List<Channel>> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            var streamsList: List<StreamDto> = emptyList()
            var errorMsg: String
            if (channelsFilter.isSubscribed) {
                val response = apiService.getSubscribedStreams()
                errorMsg = response.message()
                if (response.isSuccessful) {
                    val subscribedStreamsDto = response.getBodyOrThrow()
                    errorMsg = subscribedStreamsDto.msg
                    if (subscribedStreamsDto.result == RESULT_SUCCESS) {
                        streamsList = subscribedStreamsDto.subscriptions
                    }
                }
            } else {
                val response = apiService.getAllStreams()
                errorMsg = response.message()
                if (response.isSuccessful) {
                    val allStreamsDto = response.getBodyOrThrow()
                    errorMsg = allStreamsDto.msg
                    if (allStreamsDto.result == RESULT_SUCCESS) {
                        streamsList = allStreamsDto.streams
                    }
                }
            }
            if (streamsList.isEmpty()) {
                throw RepositoryError(errorMsg)
            }
            streamsList.toDomain(channelsFilter)
        }
    }

    override suspend fun getTopics(channel: Channel): Result<List<Topic>> =
        withContext(Dispatchers.IO) {
            runCatchingNonCancellation {
                val response = apiService.getTopics(channel.channelId)
                if (!response.isSuccessful) {
                    throw RepositoryError(response.message())
                }
                val topicsResponseDto = response.getBodyOrThrow()
                if (topicsResponseDto.result != RESULT_SUCCESS) {
                    throw RepositoryError(topicsResponseDto.msg)
                }
                val topics = mutableListOf<Topic>()
                topicsResponseDto.topics.forEach { topicDto ->
                    topics.add(Topic(topicDto.name, 0))
                }
                topics
            }
        }

    override suspend fun getTopic(filter: MessagesFilter): Result<Topic> =
        withContext(Dispatchers.IO) {
            val anchor = updateMessagesCache(filter)
            Result.success(
                Topic(
                    filter.topic.name,
                    messagesCache.getMessages(filter, true, anchor).size
                )
            )
        }

    override suspend fun sendMessage(content: String, filter: MessagesFilter): Result<Long> =
        withContext(Dispatchers.IO) {
            runCatchingNonCancellation {
                val response = apiService.sendMessageToStream(
                    filter.channel.channelId,
                    filter.topic.name,
                    content
                )
                if (!response.isSuccessful) {
                    throw RepositoryError(response.message())
                }
                val sendMessageResponse = response.getBodyOrThrow()
                if (sendMessageResponse.result != RESULT_SUCCESS) {
                    throw RepositoryError(sendMessageResponse.msg)
                }
                sendMessageResponse.messageId
            }
        }

    override suspend fun updateReaction(
        messageId: Long,
        emoji: Emoji,
        filter: MessagesFilter,
    ): Result<MessagesResult> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            val response = apiService.getSingleMessage(messageId)
            if (!response.isSuccessful) {
                throw RepositoryError(response.message())
            }
            val singleMessageResponse = response.getBodyOrThrow()
            if (singleMessageResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(singleMessageResponse.msg)
            }
            updateReaction(singleMessageResponse.message.reactions, messageId, emoji, filter)
        }
    }

    override suspend fun setMessagesFlagToRead(messageIds: List<Long>): Unit =
        withContext(Dispatchers.IO) {
            runCatchingNonCancellation {
                apiService.setMessageFlagsToRead(Json.encodeToString(messageIds))
            }
        }

    private suspend fun updateMessagesCache(
        filter: MessagesFilter,
    ): Long = runCatchingNonCancellation {
        val response = apiService.getMessages(
            anchor = ZulipApiService.ANCHOR_FIRST_UNREAD,
            narrow = filter.createNarrowJsonWithOperator()
        )
        if (response.isSuccessful) {
            val messagesResponse = response.getBodyOrThrow()
            messagesCache.addAll(messagesResponse.messages)
            if (messagesResponse.foundAnchor) messagesResponse.anchor else Message.UNDEFINED_ID
        } else {
            Message.UNDEFINED_ID
        }
    }.getOrElse {
        Message.UNDEFINED_ID
    }

    override suspend fun registerEventQueue(
        eventTypes: List<EventType>,
        messagesFilter: MessagesFilter,
    ): Result<EventsQueue> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            val response = apiService.registerEventQueue(
                narrow = messagesFilter.createNarrowJsonForEvents(),
                eventTypes = Json.encodeToString(eventTypes.toStringsList())
            )
            if (!response.isSuccessful) {
                throw RepositoryError(response.message())
            }
            val registerResponse = response.getBodyOrThrow()
            if (registerResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(registerResponse.msg)
            }
            EventsQueue(registerResponse.queueId, registerResponse.lastEventId)
        }
    }

    override suspend fun deleteEventQueue(queueId: String): Unit = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            apiService.deleteEventQueue(queueId)
        }
    }

    override suspend fun getPresenceEvents(
        queue: EventsQueue,
    ): Result<List<PresenceEvent>> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            val eventResponseBody = getNonHeartBeatEventResponse(queue)
            val eventResponse = jsonConverter.decodeFromString(
                PresenceEventsResponse.serializer(), eventResponseBody
            )
            if (eventResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(eventResponse.msg)
            }
            eventResponse.events.toDomain()
        }
    }

    override suspend fun getChannelEvents(queue: EventsQueue): Result<List<ChannelEvent>> =
        withContext(Dispatchers.IO) {
            runCatchingNonCancellation {
                val eventResponseBody = getNonHeartBeatEventResponse(queue)
                val eventResponse = jsonConverter.decodeFromString(
                    StreamEventsResponse.serializer(), eventResponseBody
                )
                if (eventResponse.result != RESULT_SUCCESS) {
                    throw RepositoryError(eventResponse.msg)
                }
                eventResponse.events.listToDomain()
            }
        }

    override suspend fun getMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
    ): Result<MessageEvent> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            val responseBody = getNonHeartBeatEventResponse(queue)
            val eventResponse = jsonConverter.decodeFromString(
                MessageEventsResponse.serializer(), responseBody
            )
            if (eventResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(eventResponse.msg)
            }
            eventResponse.events.forEach { messageEventDto ->
                messagesCache.add(messageEventDto.message)
            }
            MessageEvent(
                eventResponse.events.last().id,
                MessagesResult(
                    messagesCache.getMessages(filter).toDomain(ownUser.userId), MessagePosition()
                )
            )
        }
    }

    override suspend fun getDeleteMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
    ): Result<DeleteMessageEvent> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            val responseBody = getNonHeartBeatEventResponse(queue)
            val eventResponse = jsonConverter.decodeFromString(
                DeleteMessageEventsResponse.serializer(), responseBody
            )
            if (eventResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(eventResponse.msg)
            }
            eventResponse.events.forEach { deleteMessageEventDto ->
                messagesCache.remove(deleteMessageEventDto.messageId)
            }
            DeleteMessageEvent(
                eventResponse.events.last().id,
                MessagesResult(
                    messagesCache.getMessages(filter).toDomain(ownUser.userId), MessagePosition()
                )
            )
        }
    }

    override suspend fun getReactionEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
    ): Result<ReactionEvent> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            val responseBody = getNonHeartBeatEventResponse(queue)
            val eventResponse = jsonConverter.decodeFromString(
                ReactionEventsResponse.serializer(), responseBody
            )
            if (eventResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(eventResponse.msg)
            }
            eventResponse.events.forEach { reactionEventDto ->
                messagesCache.updateReaction(reactionEventDto)
            }
            ReactionEvent(
                eventResponse.events.last().id,
                MessagesResult(
                    messagesCache.getMessages(filter).toDomain(ownUser.userId), MessagePosition()
                )
            )
        }
    }

    private suspend fun updateReaction(
        reactions: List<ReactionDto>,
        messageId: Long,
        emoji: Emoji,
        messagesFilter: MessagesFilter,
    ): MessagesResult {
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
        return MessagesResult(
            messagesCache.getMessages(messagesFilter).toDomain(ownUser.userId),
            MessagePosition(MessagePosition.Type.EXACTLY, messageId)
        )
    }

    private suspend fun getUserPresence(userId: Long): User.Presence = runCatchingNonCancellation {
        val response = apiService.getUserPresence(userId)
        if (!response.isSuccessful) {
            return@runCatchingNonCancellation User.Presence.OFFLINE
        }

        val presenceResponse = response.getBodyOrThrow()
        if (presenceResponse.result == RESULT_SUCCESS) {
            presenceResponse.presence.toDomain()
        } else {
            User.Presence.OFFLINE
        }
    }.getOrElse {
        User.Presence.OFFLINE
    }

    private suspend fun getNonHeartBeatEventResponse(queue: EventsQueue): String {
        var lastEventId = queue.lastEventId
        var isHeartBeat = true
        var responseBody: String
        do {
            val response = apiService.getEventsFromQueue(queue.queueId, lastEventId)
            if (!response.isSuccessful) {
                throw RepositoryError(response.message())
            }
            responseBody = response.getBodyOrThrow().string()
            val heartBeatEventsResponse = jsonConverter.decodeFromString(
                HeartBeatEventsResponse.serializer(), responseBody
            )
            if (heartBeatEventsResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(heartBeatEventsResponse.msg)
            }
            heartBeatEventsResponse.events.forEach { heartBeatEventDto ->
                lastEventId = heartBeatEventDto.id
                isHeartBeat = heartBeatEventDto.type == EVENT_HEARTBEAT
            }
        } while (isHeartBeat)
        return responseBody
    }

    private fun makeAllUsersAnswer(
        usersFilter: String,
        usersResponse: AllUsersResponse,
        presencesResponse: AllPresencesResponse? = null,
    ): List<User> {
        if (usersResponse.result != RESULT_SUCCESS) {
            throw RepositoryError(usersResponse.msg)
        }

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
        return if (usersFilter.isBlank()) {
            users
        } else {
            users.filter {
                it.fullName.contains(usersFilter, true) || it.email.contains(usersFilter, true)
            }
        }
    }

    companion object {

        private const val RESULT_SUCCESS = "success"
        private const val EVENT_HEARTBEAT = "heartbeat"
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