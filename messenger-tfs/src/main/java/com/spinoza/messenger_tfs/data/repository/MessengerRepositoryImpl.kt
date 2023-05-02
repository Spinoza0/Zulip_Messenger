package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.cache.MessagesCache
import com.spinoza.messenger_tfs.data.database.MessengerDao
import com.spinoza.messenger_tfs.data.network.ZulipApiService
import com.spinoza.messenger_tfs.data.network.ZulipApiService.Companion.RESULT_SUCCESS
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
import com.spinoza.messenger_tfs.data.network.model.user.UserDto
import com.spinoza.messenger_tfs.data.network.model.user.UserResponse
import com.spinoza.messenger_tfs.data.utils.apiRequest
import com.spinoza.messenger_tfs.data.utils.createNarrowJsonForEvents
import com.spinoza.messenger_tfs.data.utils.createNarrowJsonForMessages
import com.spinoza.messenger_tfs.data.utils.dbToDomain
import com.spinoza.messenger_tfs.data.utils.dtoToDomain
import com.spinoza.messenger_tfs.data.utils.getBodyOrThrow
import com.spinoza.messenger_tfs.data.utils.isEqualTopicName
import com.spinoza.messenger_tfs.data.utils.listToDomain
import com.spinoza.messenger_tfs.data.utils.runCatchingNonCancellation
import com.spinoza.messenger_tfs.data.utils.toDbModel
import com.spinoza.messenger_tfs.data.utils.toDomain
import com.spinoza.messenger_tfs.data.utils.toDto
import com.spinoza.messenger_tfs.data.utils.toStringsList
import com.spinoza.messenger_tfs.data.utils.toUserDto
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
import com.spinoza.messenger_tfs.domain.network.ApiServiceProvider
import com.spinoza.messenger_tfs.domain.network.AppAuthKeeper
import com.spinoza.messenger_tfs.domain.repository.MessengerRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Credentials
import javax.inject.Inject

class MessengerRepositoryImpl @Inject constructor(
    private val messagesCache: MessagesCache,
    private val messengerDao: MessengerDao,
    private val apiService: ApiServiceProvider,
    private val apiAuthKeeper: AppAuthKeeper,
    private val jsonConverter: Json,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) : MessengerRepository {

    private var storedOwnUser: UserDto = UserDto()

    override suspend fun getApiKey(
        storedApiKey: String,
        email: String,
        password: String,
    ): Result<String> = withContext(ioDispatcher) {
        if (storedApiKey.isNotBlank()) {
            apiAuthKeeper.setData(Credentials.basic(email, storedApiKey))
            getOwnUser().onSuccess {
                return@withContext Result.success(storedApiKey)
            }
        }
        runCatchingNonCancellation {
            val apiKeyResponse =
                apiRequest<ApiKeyResponse> { apiService.value.fetchApiKey(email, password) }
            apiAuthKeeper.setData(Credentials.basic(apiKeyResponse.email, apiKeyResponse.apiKey))
            apiKeyResponse.apiKey
        }
    }

    override suspend fun setOwnStatusActive() {
        withContext(ioDispatcher) {
            runCatchingNonCancellation { apiService.value.setOwnStatusActive() }
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

    override suspend fun getOwnUser(): Result<User> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val ownUserResponse =
                apiRequest<OwnUserResponse> { apiService.value.getOwnUser() }
            storedOwnUser = ownUserResponse.toUserDto()
            val presence = getUserPresence(storedOwnUser.userId)
            storedOwnUser.toDomain(presence)
        }
    }

    override suspend fun getUser(userId: Long): Result<User> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val userResponse =
                apiRequest<UserResponse> { apiService.value.getUser(userId) }
            val presence = getUserPresence(userResponse.user.userId)
            userResponse.user.toDomain(presence)
        }
    }

    override suspend fun getAllUsers(): Result<List<User>> =
        withContext(ioDispatcher) {
            runCatchingNonCancellation {
                val allUsersResponse =
                    apiRequest<AllUsersResponse> { apiService.value.getAllUsers() }
                val presencesResponse = apiService.value.getAllPresences()
                if (presencesResponse.isSuccessful) {
                    makeAllUsersAnswer(allUsersResponse, presencesResponse.body())
                } else {
                    makeAllUsersAnswer(allUsersResponse)
                }
            }
        }

    override suspend fun getStoredMessages(
        filter: MessagesFilter,
    ): Result<MessagesResult> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            messagesCache.reload()
            MessagesResult(
                messagesCache.getMessages(filter).toDomain(storedOwnUser.userId),
                MessagePosition(MessagePosition.Type.LAST_POSITION)
            )
        }
    }

    override suspend fun getMessages(
        messagesPageType: MessagesPageType,
        filter: MessagesFilter,
    ): Result<MessagesResult> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            if (storedOwnUser.userId == User.UNDEFINED_ID) {
                getOwnUser()
            }
            val messagesResponse = apiRequest<MessagesResponse> {
                when (messagesPageType) {
                    MessagesPageType.FIRST_UNREAD -> apiService.value.getMessages(
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

                    MessagesPageType.LAST, MessagesPageType.STORED -> apiService.value.getMessages(
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
                messagesCache.getMessages(filter).toDomain(storedOwnUser.userId),
                position
            )
        }
    }

    override suspend fun getStoredChannels(channelsFilter: ChannelsFilter): Result<List<Channel>> =
        withContext(ioDispatcher) {
            runCatchingNonCancellation {
                val storedStreams = messengerDao.getStreams()
                storedStreams.dbToDomain(channelsFilter)
            }
        }

    override suspend fun getChannels(
        channelsFilter: ChannelsFilter,
    ): Result<List<Channel>> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val streamsList = if (channelsFilter.isSubscribed) {
                val subscribedStreamsResponse = apiService.value.getSubscribedStreams()
                subscribedStreamsResponse.subscriptions
            } else {
                val allStreamsResponse = apiService.value.getAllStreams()
                allStreamsResponse.streams
            }
            messengerDao.removeStreams(channelsFilter.isSubscribed)
            messengerDao.insertStreams(streamsList.toDbModel(channelsFilter))
            streamsList.dtoToDomain(channelsFilter)
        }
    }

    override suspend fun getStoredTopics(channel: Channel): Result<List<Topic>> =
        withContext(ioDispatcher)
        {
            runCatchingNonCancellation {
                val storedTopics = messengerDao.getTopics(channel.channelId, channel.isSubscribed)
                storedTopics.dbToDomain()
            }
        }

    override suspend fun getTopics(channel: Channel): Result<List<Topic>> =
        withContext(ioDispatcher) {
            runCatchingNonCancellation {
                val topicsResponse =
                    apiRequest<TopicsResponse> { apiService.value.getTopics(channel.channelId) }
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
                    apiService.value.getMessages(
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
                    apiService.value.getTopics(filter.channel.channelId)
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
                apiService.value.sendMessageToStream(
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
        withContext(ioDispatcher) {
            runCatchingNonCancellation {
                apiService.value.setMessageFlagsToRead(Json.encodeToString(messageIds))
            }
        }

    override suspend fun registerEventQueue(
        eventTypes: List<EventType>,
        messagesFilter: MessagesFilter,
    ): Result<EventsQueue> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val registerResponse = apiRequest<RegisterEventQueueResponse> {
                apiService.value.registerEventQueue(
                    narrow = messagesFilter.createNarrowJsonForEvents(),
                    eventTypes = Json.encodeToString(eventTypes.toStringsList())
                )
            }
            EventsQueue(registerResponse.queueId, registerResponse.lastEventId, eventTypes)
        }
    }

    override suspend fun deleteEventQueue(queueId: String): Unit = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            apiService.value.deleteEventQueue(queueId)
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
                    messagesCache.getMessages(filter).toDomain(storedOwnUser.userId),
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
            apiService.value.getMessages(numBefore, numAfter, narrow, anchorId)
        } else {
            apiService.value.getMessages(numBefore, numAfter, narrow, anchor)
        }
    }

    private fun updateReactionOnServer(messageId: Long, emoji: Emoji) {
        CoroutineScope(ioDispatcher).launch {
            runCatchingNonCancellation {
                val singleMessageResponse = apiRequest<SingleMessageResponse> {
                    apiService.value.getSingleMessage(messageId)
                }
                val isAddReaction = null == singleMessageResponse.message.reactions.find {
                    it.emojiName == emoji.name && it.userId == storedOwnUser.userId
                }
                if (isAddReaction) {
                    apiService.value.addReaction(messageId, emoji.name)
                } else {
                    apiService.value.removeReaction(messageId, emoji.name)
                }
            }
        }
    }

    private suspend fun getUserPresence(userId: Long): User.Presence = runCatchingNonCancellation {
        val presenceResponse = apiService.value.getUserPresence(userId)
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
            val response = apiService.value.getEventsFromQueue(queue.queueId, lastEventId)
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

        private const val UNKNOWN_ERROR = ""
        private const val MILLIS_IN_SECOND = 1000
        private const val OFFLINE_TIME = 180
        private const val GET_TOPIC_IGNORE_PREVIOUS_MESSAGES = 0
        private const val GET_TOPIC_MAX_UNREAD_MESSAGES_COUNT = 500
    }
}