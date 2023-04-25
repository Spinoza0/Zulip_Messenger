package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.cache.MessagesCache
import com.spinoza.messenger_tfs.data.database.MessengerDao
import com.spinoza.messenger_tfs.data.mapper.*
import com.spinoza.messenger_tfs.data.network.ZulipApiService
import com.spinoza.messenger_tfs.data.network.model.event.*
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
import okhttp3.Credentials
import javax.inject.Inject

class MessagesRepositoryImpl @Inject constructor(
    private val messagesCache: MessagesCache,
    private val messengerDao: MessengerDao,
    private val apiService: ZulipApiService,
    private val apiAuthKeeper: AppAuthKeeper,
    private val jsonConverter: Json,
) : MessagesRepository {

    private var storedOwnUser: UserDto = UserDto()

    override suspend fun getApiKey(
        storedApiKey: String,
        email: String,
        password: String,
    ): Result<String> = withContext(Dispatchers.IO) {
        if (storedApiKey.isNotBlank()) {
            apiAuthKeeper.data = Credentials.basic(email, storedApiKey)
            getOwnUser().onSuccess {
                return@withContext Result.success(storedApiKey)
            }
        }
        runCatchingNonCancellation {
            val response = apiService.fetchApiKey(email, password)
            if (!response.isSuccessful) {
                throw RepositoryError(response.message())
            }
            val apiKeyResponse = response.getBodyOrThrow()
            if (apiKeyResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(apiKeyResponse.msg)
            }
            apiAuthKeeper.data = Credentials.basic(apiKeyResponse.email, apiKeyResponse.apiKey)
            apiKeyResponse.apiKey
        }
    }

    override suspend fun setOwnStatusActive() {
        withContext(Dispatchers.IO) {
            runCatchingNonCancellation { apiService.setOwnStatusActive() }
        }
    }

    override suspend fun getOwnUserId(): Result<Long> {
        return if (storedOwnUser.userId != User.UNDEFINED_ID) {
            Result.success(storedOwnUser.userId)
        } else {
            val result = getOwnUser()
            if (result.isSuccess) {
                Result.success(storedOwnUser.userId)
            } else {
                Result.failure(result.exceptionOrNull() ?: RepositoryError(UNKNOWN_ERROR))
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
            storedOwnUser = ownUserResponse.toUserDto()
            val presence = getUserPresence(storedOwnUser.userId)
            storedOwnUser.toDomain(presence)
        }
    }

    override suspend fun getUser(userId: Long): Result<User> = withContext(Dispatchers.IO) {
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

    override suspend fun getStoredMessages(
        filter: MessagesFilter,
    ): Result<MessagesResult> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            messagesCache.reload()
            MessagesResult(
                messagesCache.getMessages(filter).toDomain(storedOwnUser.userId),
                MessagePosition(MessagePosition.Type.LAST_POSITION)
            )
        }
    }

    override suspend fun getMessages(
        messagesType: MessagesType,
        filter: MessagesFilter,
    ): Result<MessagesResult> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            if (storedOwnUser.userId == User.UNDEFINED_ID) {
                getOwnUser()
            }
            val response = when (messagesType) {
                MessagesType.FIRST_UNREAD -> apiService.getMessages(
                    numBefore = ZulipApiService.HALF_MESSAGES_PACKET,
                    numAfter = ZulipApiService.HALF_MESSAGES_PACKET,
                    narrow = filter.createNarrowJsonForMessages(),
                    anchor = ZulipApiService.ANCHOR_FIRST_UNREAD
                )
                MessagesType.NEWEST -> {
                    if (messagesCache.isNotEmpty()) {
                        apiService.getMessages(
                            numBefore = ZulipApiService.EMPTY_MESSAGES_PACKET,
                            numAfter = ZulipApiService.MAX_MESSAGES_PACKET,
                            narrow = filter.createNarrowJsonForMessages(),
                            anchor = messagesCache.lastMessageId()
                        )
                    } else {
                        apiService.getMessages(
                            numBefore = ZulipApiService.EMPTY_MESSAGES_PACKET,
                            numAfter = ZulipApiService.MAX_MESSAGES_PACKET,
                            narrow = filter.createNarrowJsonForMessages(),
                            anchor = ZulipApiService.ANCHOR_NEWEST
                        )
                    }
                }
                MessagesType.OLDEST -> {
                    if (messagesCache.isNotEmpty()) {
                        apiService.getMessages(
                            numBefore = ZulipApiService.MAX_MESSAGES_PACKET,
                            numAfter = ZulipApiService.EMPTY_MESSAGES_PACKET,
                            narrow = filter.createNarrowJsonForMessages(),
                            anchor = messagesCache.firstMessageId()
                        )
                    } else {
                        apiService.getMessages(
                            numBefore = ZulipApiService.MAX_MESSAGES_PACKET,
                            numAfter = ZulipApiService.EMPTY_MESSAGES_PACKET,
                            narrow = filter.createNarrowJsonForMessages(),
                            anchor = ZulipApiService.ANCHOR_OLDEST
                        )
                    }
                }
                MessagesType.LAST, MessagesType.STORED -> apiService.getMessages(
                    numBefore = ZulipApiService.MAX_MESSAGES_PACKET,
                    numAfter = ZulipApiService.EMPTY_MESSAGES_PACKET,
                    narrow = filter.createNarrowJsonForMessages(),
                    anchor = ZulipApiService.ANCHOR_NEWEST
                )
            }
            if (!response.isSuccessful) {
                throw RepositoryError(response.message())
            }
            val messagesResponse = response.getBodyOrThrow()
            if (messagesResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(messagesResponse.msg)
            }
            val position = when (messagesType) {
                MessagesType.FIRST_UNREAD -> if (messagesResponse.foundAnchor) {
                    MessagePosition(MessagePosition.Type.EXACTLY, messagesResponse.anchor)
                } else {
                    MessagePosition(MessagePosition.Type.LAST_POSITION)
                }
                MessagesType.LAST -> MessagePosition(MessagePosition.Type.LAST_POSITION)
                else -> MessagePosition(MessagePosition.Type.UNDEFINED)
            }
            messagesCache.addAll(messagesResponse.messages, messagesType)
            MessagesResult(
                messagesCache.getMessages(filter).toDomain(storedOwnUser.userId),
                position
            )
        }
    }

    override suspend fun getStoredChannels(channelsFilter: ChannelsFilter): Result<List<Channel>> =
        withContext(Dispatchers.IO) {
            runCatchingNonCancellation {
                val storedStreams = messengerDao.getStreams()
                storedStreams.dbToDomain(channelsFilter)
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
            messengerDao.removeStreams(channelsFilter.isSubscribed)
            messengerDao.insertStreams(streamsList.toDbModel(channelsFilter))
            streamsList.dtoToDomain(channelsFilter)
        }
    }

    override suspend fun getStoredTopics(channel: Channel): Result<List<Topic>> =
        withContext(Dispatchers.IO)
        {
            runCatchingNonCancellation {
                val storedTopics = messengerDao.getTopics(channel.channelId, channel.isSubscribed)
                storedTopics.dbToDomain()
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
                messengerDao.removeTopics(channel.channelId, channel.isSubscribed)
                messengerDao.insertTopics(topicsResponseDto.topics.toDbModel(channel))
                topicsResponseDto.topics.dtoToDomain(channel)
            }
        }

    override suspend fun getTopic(filter: MessagesFilter): Result<Topic> =
        withContext(Dispatchers.IO) {
            var unreadMessagesCount = 0
            var lastMessageId = Message.UNDEFINED_ID
            runCatchingNonCancellation {
                val response = apiService.getMessages(
                    numBefore = GET_TOPIC_IGNORE_PREVIOUS_MESSAGES,
                    numAfter = GET_TOPIC_MAX_UNREAD_MESSAGES_COUNT,
                    narrow = filter.createNarrowJsonForMessages(),
                    anchor = ZulipApiService.ANCHOR_FIRST_UNREAD,
                )
                if (response.isSuccessful) {
                    val messagesResponse = response.getBodyOrThrow()
                    lastMessageId = messagesResponse.messages.last().id
                    unreadMessagesCount = messagesResponse.messages.size
                    if (!messagesResponse.foundAnchor) unreadMessagesCount--
                }
            }
            Result.success(
                Topic(
                    filter.topic.name, unreadMessagesCount, filter.channel.channelId, lastMessageId
                )
            )
        }

    override suspend fun sendMessage(
        content: String,
        filter: MessagesFilter,
    ): Result<MessagesResult> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            val response =
                apiService.sendMessageToStream(filter.channel.channelId, filter.topic.name, content)
            if (!response.isSuccessful) {
                throw RepositoryError(response.message())
            }
            val sendMessageResponse = response.getBodyOrThrow()
            if (sendMessageResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(sendMessageResponse.msg)
            }
            var messagesResult = MessagesResult(emptyList(), MessagePosition())
            getMessages(MessagesType.LAST, filter)
                .onSuccess { messagesResult = it }
                .onFailure { throw it }
            messagesResult
        }
    }

    override suspend fun updateReaction(
        messageId: Long,
        emoji: Emoji,
        filter: MessagesFilter,
    ): Result<MessagesResult> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            messagesCache
                .updateReaction(messageId, storedOwnUser.userId, emoji.toDto(storedOwnUser.userId))
            val result = MessagesResult(
                messagesCache.getMessages(filter).toDomain(storedOwnUser.userId),
                MessagePosition(MessagePosition.Type.EXACTLY, messageId)
            )
            updateReactionOnServer(messageId, emoji)
            result
        }
    }

    override suspend fun setMessagesFlagToRead(messageIds: List<Long>): Unit =
        withContext(Dispatchers.IO) {
            runCatchingNonCancellation {
                apiService.setMessageFlagsToRead(Json.encodeToString(messageIds))
            }
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
            EventsQueue(registerResponse.queueId, registerResponse.lastEventId, eventTypes)
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

    override suspend fun getChannelEvents(
        queue: EventsQueue,
        channelsFilter: ChannelsFilter,
    ): Result<List<ChannelEvent>> =
        withContext(Dispatchers.IO) {
            runCatchingNonCancellation {
                val eventResponseBody = getNonHeartBeatEventResponse(queue)
                val eventResponse = jsonConverter.decodeFromString(
                    StreamEventsResponse.serializer(), eventResponseBody
                )
                if (eventResponse.result != RESULT_SUCCESS) {
                    throw RepositoryError(eventResponse.msg)
                }
                eventResponse.events.listToDomain(channelsFilter)
            }
        }

    override suspend fun getMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<MessageEvent> = withContext(Dispatchers.IO) {
        runCatchingNonCancellation {
            val responseBody = getNonHeartBeatEventResponse(queue)
            val eventResponse = jsonConverter.decodeFromString(
                MessageEventsResponse.serializer(), responseBody
            )
            if (eventResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(eventResponse.msg)
            }
            if (messagesCache.isNotEmpty() &&
                filter.topic.lastMessageId == messagesCache.lastMessageId()
            ) {
                eventResponse.events.forEach { messageEventDto ->
                    messagesCache.add(messageEventDto.message, isLastMessageVisible)
                    filter.topic.lastMessageId = messageEventDto.message.id
                }
            }
            MessageEvent(
                eventResponse.events.last().id,
                MessagesResult(
                    messagesCache.getMessages(filter).toDomain(storedOwnUser.userId),
                    MessagePosition(),
                    eventResponse.events.isNotEmpty()
                )
            )
        }
    }

    override suspend fun getDeleteMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
        isLastMessageVisible: Boolean,
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
                    messagesCache.getMessages(filter).toDomain(storedOwnUser.userId),
                    MessagePosition()
                )
            )
        }
    }

    override suspend fun getReactionEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
        isLastMessageVisible: Boolean,
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
                    messagesCache.getMessages(filter).toDomain(storedOwnUser.userId),
                    MessagePosition()
                )
            )
        }
    }

    private fun updateReactionOnServer(messageId: Long, emoji: Emoji) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatchingNonCancellation {
                val response = apiService.getSingleMessage(messageId)
                if (!response.isSuccessful) {
                    throw RepositoryError(response.message())
                }
                val singleMessageResponse = response.getBodyOrThrow()
                if (singleMessageResponse.result != RESULT_SUCCESS) {
                    throw RepositoryError(singleMessageResponse.msg)
                }
                val isAddReaction = null == singleMessageResponse.message.reactions.find {
                    it.emojiName == emoji.name && it.userId == storedOwnUser.userId
                }
                if (isAddReaction) {
                    apiService.addReaction(messageId, emoji.name)
                } else {
                    apiService.removeReaction(messageId, emoji.name)
                }
            }
        }
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
        private const val UNKNOWN_ERROR = ""
        private const val MILLIS_IN_SECOND = 1000
        private const val OFFLINE_TIME = 180
        private const val GET_TOPIC_IGNORE_PREVIOUS_MESSAGES = 0
        private const val GET_TOPIC_MAX_UNREAD_MESSAGES_COUNT = 500
    }
}