package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.cache.MessagesCache
import com.spinoza.messenger_tfs.data.database.MessengerDao
import com.spinoza.messenger_tfs.data.network.apiservice.ZulipApiService
import com.spinoza.messenger_tfs.data.network.apiservice.ZulipApiService.Companion.RESULT_SUCCESS
import com.spinoza.messenger_tfs.data.network.model.ApiKeyResponse
import com.spinoza.messenger_tfs.data.network.model.event.DeleteMessageEventsResponse
import com.spinoza.messenger_tfs.data.network.model.event.HeartBeatEventsResponse
import com.spinoza.messenger_tfs.data.network.model.event.MessageEventsResponse
import com.spinoza.messenger_tfs.data.network.model.event.PresenceEventsResponse
import com.spinoza.messenger_tfs.data.network.model.event.ReactionEventsResponse
import com.spinoza.messenger_tfs.data.network.model.event.RegisterEventQueueResponse
import com.spinoza.messenger_tfs.data.network.model.event.StreamEventsResponse
import com.spinoza.messenger_tfs.data.network.model.message.MessagesResponse
import com.spinoza.messenger_tfs.data.network.model.message.SendMessageResponse
import com.spinoza.messenger_tfs.data.network.model.message.SingleMessageResponse
import com.spinoza.messenger_tfs.data.network.model.presence.AllPresencesResponse
import com.spinoza.messenger_tfs.data.network.model.stream.TopicsResponse
import com.spinoza.messenger_tfs.data.network.model.user.AllUsersResponse
import com.spinoza.messenger_tfs.data.network.model.user.OwnUserResponse
import com.spinoza.messenger_tfs.data.network.model.user.UserResponse
import com.spinoza.messenger_tfs.data.utils.apiRequest
import com.spinoza.messenger_tfs.data.utils.createNarrowJsonForEvents
import com.spinoza.messenger_tfs.data.utils.createNarrowJsonForMessages
import com.spinoza.messenger_tfs.data.utils.dtoToDomain
import com.spinoza.messenger_tfs.data.utils.getBodyOrThrow
import com.spinoza.messenger_tfs.data.utils.isEqualTopicName
import com.spinoza.messenger_tfs.data.utils.listToDomain
import com.spinoza.messenger_tfs.data.utils.runCatchingNonCancellation
import com.spinoza.messenger_tfs.data.utils.toDbModel
import com.spinoza.messenger_tfs.data.utils.toDomain
import com.spinoza.messenger_tfs.data.utils.toDto
import com.spinoza.messenger_tfs.data.utils.toStringsList
import com.spinoza.messenger_tfs.di.DispatcherIO
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesPageType
import com.spinoza.messenger_tfs.domain.model.MessagesResult
import com.spinoza.messenger_tfs.domain.model.RepositoryError
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.model.event.ChannelEvent
import com.spinoza.messenger_tfs.domain.model.event.DeleteMessageEvent
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.model.event.MessageEvent
import com.spinoza.messenger_tfs.domain.model.event.PresenceEvent
import com.spinoza.messenger_tfs.domain.model.event.ReactionEvent
import com.spinoza.messenger_tfs.domain.network.AuthorizationStorage
import com.spinoza.messenger_tfs.domain.repository.WebRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class WebRepositoryImpl @Inject constructor(
    private val messagesCache: MessagesCache,
    private val messengerDao: MessengerDao,
    private val apiService: ZulipApiService,
    private val authorizationStorage: AuthorizationStorage,
    private val jsonConverter: Json,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) : WebRepository {

    override suspend fun getLoggedInUserId(email: String, password: String): Result<Long> =
        withContext(ioDispatcher) {
            if (authorizationStorage.makeAuthHeader(email).isNotBlank()) getOwnUser().onSuccess {
                authorizationStorage.saveData(it.userId, email, password)
                return@withContext Result.success(authorizationStorage.getUserId())
            }
            runCatchingNonCancellation {
                val apiKeyResponse =
                    apiRequest<ApiKeyResponse> { apiService.fetchApiKey(email, password) }
                authorizationStorage.makeAuthHeader(apiKeyResponse.email, apiKeyResponse.apiKey)
                getOwnUser().onSuccess {
                    authorizationStorage.saveData(
                        it.userId,
                        apiKeyResponse.email,
                        password,
                        apiKeyResponse.apiKey
                    )
                    return@runCatchingNonCancellation authorizationStorage.getUserId()
                }
                User.UNDEFINED_ID
            }
        }

    override suspend fun setOwnStatusActive() {
        withContext(ioDispatcher) {
            runCatchingNonCancellation { apiService.setOwnStatusActive() }
        }
    }

    override suspend fun getOwnUser(): Result<User> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val ownUserResponse = apiRequest<OwnUserResponse> { apiService.getOwnUser() }
            val presence = getUserPresence(ownUserResponse.userId)
            ownUserResponse.toDomain(presence)
        }
    }

    override suspend fun getUser(userId: Long): Result<User> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val userResponse = apiRequest<UserResponse> { apiService.getUser(userId) }
            val presence = getUserPresence(userResponse.user.userId)
            userResponse.user.toDomain(presence)
        }
    }

    override suspend fun getAllUsers(): Result<List<User>> =
        withContext(ioDispatcher) {
            runCatchingNonCancellation {
                val allUsersResponse = apiRequest<AllUsersResponse> { apiService.getAllUsers() }
                val presencesResponse = apiService.getAllPresences()
                if (presencesResponse.isSuccessful) {
                    makeAllUsersAnswer(allUsersResponse, presencesResponse.body())
                } else {
                    makeAllUsersAnswer(allUsersResponse)
                }
            }
        }

    override suspend fun getMessages(
        messagesPageType: MessagesPageType,
        filter: MessagesFilter,
    ): Result<MessagesResult> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val messagesResponse = apiRequest<MessagesResponse> {
                when (messagesPageType) {
                    MessagesPageType.FIRST_UNREAD -> apiService.getMessages(
                        numBefore = ZulipApiService.HALF_MESSAGES_PACKET,
                        numAfter = ZulipApiService.HALF_MESSAGES_PACKET,
                        narrow = filter.createNarrowJsonForMessages(),
                        anchor = ZulipApiService.ANCHOR_FIRST_UNREAD
                    )

                    MessagesPageType.NEWEST -> apiGetMessages(
                        numBefore = ZulipApiService.EMPTY_MESSAGES_PACKET,
                        numAfter = ZulipApiService.MAX_MESSAGES_PACKET,
                        narrow = filter.createNarrowJsonForMessages(),
                        anchorId = messagesCache.getLastMessageId(filter),
                        ZulipApiService.ANCHOR_NEWEST
                    )

                    MessagesPageType.OLDEST -> apiGetMessages(
                        numBefore = ZulipApiService.MAX_MESSAGES_PACKET,
                        numAfter = ZulipApiService.EMPTY_MESSAGES_PACKET,
                        narrow = filter.createNarrowJsonForMessages(),
                        anchorId = messagesCache.getFirstMessageId(filter),
                        ZulipApiService.ANCHOR_OLDEST
                    )

                    MessagesPageType.AFTER_STORED -> apiGetMessages(
                        numBefore = ZulipApiService.HALF_MESSAGES_PACKET,
                        numAfter = ZulipApiService.HALF_MESSAGES_PACKET,
                        narrow = filter.createNarrowJsonForMessages(),
                        anchorId = messagesCache.getLastMessageId(filter),
                        ZulipApiService.ANCHOR_NEWEST
                    )

                    MessagesPageType.LAST, MessagesPageType.STORED -> apiService.getMessages(
                        numBefore = ZulipApiService.MAX_MESSAGES_PACKET,
                        numAfter = ZulipApiService.EMPTY_MESSAGES_PACKET,
                        narrow = filter.createNarrowJsonForMessages(),
                        anchor = ZulipApiService.ANCHOR_NEWEST
                    )
                }
            }
            val position = when (messagesPageType) {
                MessagesPageType.FIRST_UNREAD -> if (messagesResponse.foundAnchor) {
                    MessagePosition(MessagePosition.Type.EXACTLY, messagesResponse.anchor)
                } else {
                    MessagePosition(MessagePosition.Type.LAST_POSITION)
                }

                MessagesPageType.LAST -> MessagePosition(MessagePosition.Type.LAST_POSITION)
                else -> MessagePosition(MessagePosition.Type.UNDEFINED)
            }
            messagesCache.addAll(messagesResponse.messages, messagesPageType)
            MessagesResult(
                messagesCache.getMessages(filter).toDomain(authorizationStorage.getUserId()),
                position
            )
        }
    }

    override suspend fun getChannels(
        channelsFilter: ChannelsFilter,
    ): Result<List<Channel>> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val streamsList = if (channelsFilter.isSubscribed) {
                val subscribedStreamsResponse = apiService.getSubscribedStreams()
                subscribedStreamsResponse.subscriptions
            } else {
                val allStreamsResponse = apiService.getAllStreams()
                allStreamsResponse.streams
            }
            messengerDao.removeStreams(channelsFilter.isSubscribed)
            messengerDao.insertStreams(streamsList.toDbModel(channelsFilter))
            streamsList.dtoToDomain(channelsFilter)
        }
    }

    override suspend fun getTopics(channel: Channel): Result<List<Topic>> =
        withContext(ioDispatcher) {
            runCatchingNonCancellation {
                val topicsResponse =
                    apiRequest<TopicsResponse> { apiService.getTopics(channel.channelId) }
                messengerDao.removeTopics(channel.channelId, channel.isSubscribed)
                messengerDao.insertTopics(topicsResponse.topics.toDbModel(channel))
                topicsResponse.topics.dtoToDomain(channel)
            }
        }

    override suspend fun getTopic(filter: MessagesFilter): Result<Topic> =
        withContext(ioDispatcher) {
            var unreadMessagesCount = 0
            var lastMessageId = Message.UNDEFINED_ID
            runCatchingNonCancellation {
                val messagesResponse = apiRequest<MessagesResponse> {
                    apiService.getMessages(
                        numBefore = GET_TOPIC_IGNORE_PREVIOUS_MESSAGES,
                        numAfter = GET_TOPIC_MAX_UNREAD_MESSAGES_COUNT,
                        narrow = filter.createNarrowJsonForMessages(),
                        anchor = ZulipApiService.ANCHOR_FIRST_UNREAD,
                    )
                }
                if (messagesResponse.messages.isNotEmpty()) {
                    lastMessageId = messagesResponse.messages.last().id
                    unreadMessagesCount = messagesResponse.messages.size
                    if (!messagesResponse.foundAnchor) {
                        unreadMessagesCount--
                    }
                }
            }
            Result.success(
                Topic(
                    filter.topic.name, unreadMessagesCount, filter.channel.channelId, lastMessageId
                )
            )
        }

    override suspend fun getUpdatedMessageFilter(filter: MessagesFilter): MessagesFilter =
        withContext(ioDispatcher) {
            var topic = filter.topic
            runCatchingNonCancellation {
                val topicsResponse = apiRequest<TopicsResponse> {
                    apiService.getTopics(filter.channel.channelId)
                }
                val newTopic = topicsResponse.topics.find {
                    filter.isEqualTopicName(it.name)
                }
                if (newTopic != null) {
                    topic = topic.copy(lastMessageId = newTopic.maxId)
                }
            }
            filter.copy(topic = topic)
        }

    override suspend fun sendMessage(
        content: String,
        filter: MessagesFilter,
    ): Result<Long> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val sendMessageResponse = apiRequest<SendMessageResponse> {
                apiService.sendMessageToStream(
                    filter.channel.channelId,
                    filter.topic.name,
                    content
                )
            }
            sendMessageResponse.messageId
        }
    }

    override suspend fun updateReaction(
        messageId: Long,
        emoji: Emoji,
        filter: MessagesFilter,
    ): Result<MessagesResult> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val ownUserId = authorizationStorage.getUserId()
            messagesCache.updateReaction(messageId, ownUserId, emoji.toDto(ownUserId))
            val result = MessagesResult(
                messagesCache.getMessages(filter).toDomain(ownUserId),
                MessagePosition(MessagePosition.Type.EXACTLY, messageId)
            )
            updateReactionOnServer(messageId, emoji)
            result
        }
    }

    override suspend fun setMessagesFlagToRead(messageIds: List<Long>): Unit =
        withContext(ioDispatcher) {
            runCatchingNonCancellation {
                apiService.setMessageFlagsToRead(Json.encodeToString(messageIds))
            }
        }

    override suspend fun registerEventQueue(
        eventTypes: List<EventType>,
        messagesFilter: MessagesFilter,
    ): Result<EventsQueue> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val registerResponse = apiRequest<RegisterEventQueueResponse> {
                apiService.registerEventQueue(
                    narrow = messagesFilter.createNarrowJsonForEvents(),
                    eventTypes = Json.encodeToString(eventTypes.toStringsList())
                )
            }
            EventsQueue(registerResponse.queueId, registerResponse.lastEventId, eventTypes)
        }
    }

    override suspend fun deleteEventQueue(queueId: String): Unit = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            apiService.deleteEventQueue(queueId)
        }
    }

    override suspend fun getPresenceEvents(
        queue: EventsQueue,
    ): Result<List<PresenceEvent>> = withContext(ioDispatcher) {
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
        withContext(ioDispatcher) {
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
    ): Result<MessageEvent> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val responseBody = getNonHeartBeatEventResponse(queue)
            val eventResponse = jsonConverter.decodeFromString(
                MessageEventsResponse.serializer(), responseBody
            )
            if (eventResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(eventResponse.msg)
            }
            if (messagesCache.isNotEmpty() &&
                filter.topic.lastMessageId == messagesCache.getLastMessageId(filter)
            ) {
                eventResponse.events.forEach { messageEventDto ->
                    messagesCache.add(messageEventDto.message, isLastMessageVisible)
                }
            }
            MessageEvent(
                eventResponse.events.last().id,
                MessagesResult(
                    messagesCache.getMessages(filter).toDomain(authorizationStorage.getUserId()),
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
    ): Result<DeleteMessageEvent> = withContext(ioDispatcher) {
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
                    messagesCache.getMessages(filter).toDomain(authorizationStorage.getUserId()),
                    MessagePosition()
                )
            )
        }
    }

    override suspend fun getReactionEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<ReactionEvent> = withContext(ioDispatcher) {
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
                    messagesCache.getMessages(filter).toDomain(authorizationStorage.getUserId()),
                    MessagePosition()
                )
            )
        }
    }

    private suspend fun apiGetMessages(
        numBefore: Int,
        numAfter: Int,
        narrow: String,
        anchorId: Long,
        anchor: String,
    ): MessagesResponse {
        return if (anchorId != Message.UNDEFINED_ID) {
            apiService.getMessages(numBefore, numAfter, narrow, anchorId)
        } else {
            apiService.getMessages(numBefore, numAfter, narrow, anchor)
        }
    }

    private fun updateReactionOnServer(messageId: Long, emoji: Emoji) {
        CoroutineScope(ioDispatcher).launch {
            runCatchingNonCancellation {
                val singleMessageResponse = apiRequest<SingleMessageResponse> {
                    apiService.getSingleMessage(messageId)
                }
                val ownUserId = authorizationStorage.getUserId()
                val isAddReaction = null == singleMessageResponse.message.reactions.find {
                    it.emojiName == emoji.name && it.userId == ownUserId
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
        val presenceResponse = apiService.getUserPresence(userId)
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
                isHeartBeat = heartBeatEventDto.type == ZulipApiService.EVENT_HEARTBEAT
            }
        } while (isHeartBeat)
        return responseBody
    }

    private fun makeAllUsersAnswer(
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
        return users
    }

    companion object {

        private const val MILLIS_IN_SECOND = 1000
        private const val OFFLINE_TIME = 180
        private const val GET_TOPIC_IGNORE_PREVIOUS_MESSAGES = 0
        private const val GET_TOPIC_MAX_UNREAD_MESSAGES_COUNT = 500
    }
}