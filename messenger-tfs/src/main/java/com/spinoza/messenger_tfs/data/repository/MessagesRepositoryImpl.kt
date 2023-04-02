package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.listToDomain
import com.spinoza.messenger_tfs.data.model.event.PresenceEventsResponse
import com.spinoza.messenger_tfs.data.model.event.StreamEventsResponse
import com.spinoza.messenger_tfs.data.model.message.MessagesResponse
import com.spinoza.messenger_tfs.data.model.message.NarrowItemDto
import com.spinoza.messenger_tfs.data.model.message.ReactionDto
import com.spinoza.messenger_tfs.data.model.presence.AllPresencesResponse
import com.spinoza.messenger_tfs.data.model.stream.StreamDto
import com.spinoza.messenger_tfs.data.model.user.AllUsersResponse
import com.spinoza.messenger_tfs.data.model.user.UserDto
import com.spinoza.messenger_tfs.data.network.ZulipApiFactory
import com.spinoza.messenger_tfs.data.toDomain
import com.spinoza.messenger_tfs.data.toUserDto
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.model.event.ChannelEvent
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.model.event.PresenceEvent
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.Response


// TODO: 1) отрефакторить - не все сообщения сразу отправлять, а только новые или измененные
// TODO: 2) отрефакторить - кэш (messagesLocalCache) вынести в отдельный класс

class MessagesRepositoryImpl private constructor() : MessagesRepository {

    private var ownUser: UserDto = UserDto()
    private var isOwnUserLoaded = false
    private val apiService = ZulipApiFactory.apiService
    private val jsonConverter = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun getOwnUserId(): RepositoryResult<Long> {
        return if (isOwnUserLoaded) {
            RepositoryResult.Success(ownUser.userId)
        } else
            when (val result = getOwnUser()) {
                is RepositoryResult.Success -> RepositoryResult.Success(ownUser.userId)
                is RepositoryResult.Failure.OwnUserNotFound -> RepositoryResult.Failure.OwnUserNotFound(
                    result.value
                )
                is RepositoryResult.Failure.Network -> RepositoryResult.Failure.Network(result.value)
                else -> RepositoryResult.Failure.Network("")
            }
    }

    override suspend fun getOwnUser(): RepositoryResult<User> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getOwnUser()
            when (response.isSuccessful) {
                true -> {
                    val ownResponseDto = response.getBodyOrThrow()
                    when (ownResponseDto.result) {
                        RESULT_SUCCESS -> {
                            ownUser = ownResponseDto.toUserDto()
                            val presence = getUserPresence(ownUser.userId)
                            RepositoryResult.Success(ownUser.toDomain(presence))
                        }
                        else -> RepositoryResult.Failure.OwnUserNotFound(ownResponseDto.msg)
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
                        val userResponseDto = response.getBodyOrThrow()
                        when (userResponseDto.result) {
                            RESULT_SUCCESS -> {
                                val presence = getUserPresence(userResponseDto.user.userId)
                                RepositoryResult.Success(userResponseDto.user.toDomain(presence))
                            }
                            else -> RepositoryResult.Failure.UserNotFound(
                                userId,
                                userResponseDto.msg
                            )
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
                val usersResponse = apiService.getAllUsers()
                when (usersResponse.isSuccessful) {
                    true ->
                        handleGetUsersByFilterResult(usersResponse.getBodyOrThrow(), usersFilter)
                    false -> RepositoryResult.Failure.LoadingUsers(usersResponse.message())
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
                false -> RepositoryResult.Failure.LoadingMessages(
                    messagesFilter,
                    response.message()
                )
            }
        }.getOrElse {
            RepositoryResult.Failure.Network(getErrorText(it))
        }
    }

    private fun handleGetMessagesResult(
        messagesResponse: MessagesResponse,
        messageId: Long,
        messagesFilter: MessagesFilter,
    ) = when (messagesResponse.result) {
        RESULT_SUCCESS -> {
            val positionType =
                if (messageId != Message.UNDEFINED_ID) {
                    if (messagesResponse.messages.last().id == messageId) {
                        MessagePosition.Type.LAST_POSITION
                    } else {
                        MessagePosition.Type.EXACTLY
                    }
                } else {
                    MessagePosition.Type.UNDEFINED
                }
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
                                RepositoryResult.Success(
                                    topicsResponseDto.topics.toDomain(
                                        getMessages(MessagesFilter(channel, Topic("", 0)))
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
        val messagesResult = getMessages(messagesFilter)
        if (messagesResult is RepositoryResult.Success) {
            RepositoryResult.Success(
                Topic(
                    messagesFilter.topic.name,
                    messagesResult.value.messages.size
                )
            )
        } else {
            RepositoryResult.Failure.LoadingTopicData(messagesFilter)
        }
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
                    val messagesResponseDto = response.getBodyOrThrow()
                    when (messagesResponseDto.result) {
                        RESULT_SUCCESS -> {
                            val messageId = messagesResponseDto.messageId ?: Message.UNDEFINED_ID
                            getMessages(messagesFilter, messageId)
                        }
                        else -> RepositoryResult.Failure.SendingMessage(messagesResponseDto.msg)
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
                    val singleMessageResponseDto = response.getBodyOrThrow()
                    when (singleMessageResponseDto.result) {
                        RESULT_SUCCESS -> {
                            updateReaction(
                                singleMessageResponseDto.message.reactions,
                                messageId,
                                emoji,
                                messagesFilter
                            )
                        }
                        else ->
                            RepositoryResult.Failure.UpdatingReaction(singleMessageResponseDto.msg)
                    }
                }
                false -> RepositoryResult.Failure.UpdatingReaction(response.message())
            }
        }.getOrElse {
            RepositoryResult.Failure.UpdatingReaction(getErrorText(it))
        }
    }

    override suspend fun registerEventQueue(eventType: EventType): RepositoryResult<EventsQueue> =
        withContext(Dispatchers.IO) {
            runCatching {
                val eventTypeString = when (eventType) {
                    EventType.PRESENCE -> EVENT_TYPE_PRESENCE
                    EventType.CHANNEL -> EVENT_TYPE_STREAM
                }
                val response = apiService.registerEventQueue(
                    eventTypes = Json.encodeToString(listOf(eventTypeString))
                )
                when (response.isSuccessful) {
                    true -> {
                        val registerResponse = response.getBodyOrThrow()
                        when (registerResponse.result) {
                            RESULT_SUCCESS -> RepositoryResult.Success(
                                EventsQueue(
                                    registerResponse.queueId,
                                    registerResponse.lastEventId
                                )
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
                    }
                }
                false -> RepositoryResult.Failure.GetEvents(response.message())
            }
        }.getOrElse {
            RepositoryResult.Failure.Network(getErrorText(it))
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

    private suspend fun updateReaction(
        reactions: List<ReactionDto>,
        messageId: Long,
        emoji: Emoji,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<MessagesResult> = runCatching {
        val isAddReaction = null == reactions.find {
            it.emoji_name == emoji.name && it.user_id == ownUser.userId
        }
        val response = if (isAddReaction) {
            apiService.addReaction(messageId, emoji.name)
        } else {
            apiService.removeReaction(messageId, emoji.name)
        }
        when (response.isSuccessful) {
            true -> {
                val updateReactionResponseDto = response.getBodyOrThrow()
                when (updateReactionResponseDto.result) {
                    RESULT_SUCCESS -> getMessages(messagesFilter, messageId)
                    else -> RepositoryResult.Failure.UpdatingReaction(updateReactionResponseDto.msg)
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
                val presenceResponseDto = response.getBodyOrThrow()
                when (presenceResponseDto.result) {
                    RESULT_SUCCESS -> presenceResponseDto.presence.toDomain()
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
        usersResponseDto: AllUsersResponse,
        presencesResponseDto: AllPresencesResponse? = null,
    ): RepositoryResult<List<User>> = if (usersResponseDto.result == RESULT_SUCCESS) {
        val users = mutableListOf<User>()
        usersResponseDto.members
            .filter { it.isBot.not() }
            .forEach { userDto ->
                val presence =
                    if (presencesResponseDto != null && presencesResponseDto.result == RESULT_SUCCESS) {
                        presencesResponseDto.presences[userDto.email]?.toDomain()
                            ?: User.Presence.OFFLINE
                    } else {
                        User.Presence.OFFLINE
                    }
                users.add(userDto.toDomain(presence))
            }
        val result = if (usersFilter.isBlank()) users
        else users.filter { it.fullName.contains(usersFilter, true) }
        RepositoryResult.Success(result)
    } else {
        RepositoryResult.Failure.LoadingUsers(usersResponseDto.msg)
    }

    private inline fun <reified T> Response<T>?.getBodyOrThrow(): T {
        return this?.body() ?: throw RuntimeException("Empty response body")
    }

    private fun MessagesFilter.createNarrow(): String {
        val narrowDtoList = mutableListOf(
            NarrowItemDto(OPERATOR_STREAM, channel.name)
        )
        if (topic.name.isNotEmpty()) {
            narrowDtoList.add(NarrowItemDto(OPERATOR_TOPIC, topic.name))
        }
        return Json.encodeToString(narrowDtoList)
    }

    private fun getErrorText(e: Throwable): String =
        e.localizedMessage ?: e.message ?: e.toString()

    companion object {

        private const val RESULT_SUCCESS = "success"
        private const val OPERATOR_STREAM = "stream"
        private const val OPERATOR_TOPIC = "topic"

        private const val EVENT_TYPE_PRESENCE = "presence"
        private const val EVENT_TYPE_STREAM = "stream"

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