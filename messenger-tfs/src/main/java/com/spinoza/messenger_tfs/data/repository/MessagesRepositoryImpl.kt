package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.model.message.MessagesResponseDto
import com.spinoza.messenger_tfs.data.model.message.NarrowItemDto
import com.spinoza.messenger_tfs.data.model.message.ReactionDto
import com.spinoza.messenger_tfs.data.model.presence.AllPresencesResponseDto
import com.spinoza.messenger_tfs.data.model.stream.StreamDto
import com.spinoza.messenger_tfs.data.model.user.AllUsersResponseDto
import com.spinoza.messenger_tfs.data.model.user.UserDto
import com.spinoza.messenger_tfs.data.network.ZulipApiFactory
import com.spinoza.messenger_tfs.data.toDomain
import com.spinoza.messenger_tfs.data.toUserDto
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.repository.MessagePosition
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.MessagesResult
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Credentials
import retrofit2.Response


// TODO: 1) отрефакторить - не все сообщения сразу отправлять, а только новые или измененные
// TODO: 2) отрефакторить - кэш (messagesLocalCache) вынести в отдельный класс

class MessagesRepositoryImpl private constructor() : MessagesRepository {

    private var ownUser: UserDto = UserDto()
    private var isOwnUserLoaded = false
    private val authHeader = Credentials.basic(CREDENTIALS_USERNAME, CREDENTIALS_PASSWORD)
    private val apiService = ZulipApiFactory.apiService

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
            val response = apiService.getOwnUser(authHeader)
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
                val response = apiService.getUser(authHeader, userId)
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
                val usersResponse = apiService.getAllUsers(authHeader)
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
        allUsersResponseDto: AllUsersResponseDto,
        usersFilter: String,
    ) = when (allUsersResponseDto.result) {
        RESULT_SUCCESS -> {
            val presencesResponse = apiService.getAllPresences(authHeader)
            when (presencesResponse.isSuccessful) {
                true -> makeAllUsersAnswer(
                    usersFilter,
                    allUsersResponseDto,
                    presencesResponse.body()
                )
                false -> makeAllUsersAnswer(usersFilter, allUsersResponseDto)
            }
        }
        else -> RepositoryResult.Failure.LoadingUsers(allUsersResponseDto.msg)
    }

    override suspend fun getMessages(
        messagesFilter: MessagesFilter,
        messageId: Long,
    ): RepositoryResult<MessagesResult> = withContext(Dispatchers.IO) {
        runCatching {
            val response =
                apiService.getMessages(authHeader, narrow = messagesFilter.createNarrow())
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
        messagesResponseDto: MessagesResponseDto,
        messageId: Long,
        messagesFilter: MessagesFilter,
    ) = when (messagesResponseDto.result) {
        RESULT_SUCCESS -> {
            val positionType =
                if (messageId != Message.UNDEFINED_ID) {
                    if (messagesResponseDto.messages.last().id == messageId) {
                        MessagePosition.Type.LAST_POSITION
                    } else {
                        MessagePosition.Type.EXACTLY
                    }
                } else {
                    MessagePosition.Type.UNDEFINED
                }
            RepositoryResult.Success(
                MessagesResult(
                    messagesResponseDto.messages.toDomain(ownUser.userId),
                    MessagePosition(positionType, messageId)
                )
            )
        }
        else -> RepositoryResult.Failure.LoadingMessages(
            messagesFilter,
            messagesResponseDto.msg
        )
    }

    override suspend fun getChannels(
        channelsFilter: ChannelsFilter,
    ): RepositoryResult<List<Channel>> = withContext(Dispatchers.IO) {
        runCatching {
            var streamsList: List<StreamDto> = emptyList()
            var errorMsg = ""
            if (channelsFilter.isSubscribed) {
                val response = apiService.getSubscribedStreams(authHeader)
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
                val response = apiService.getAllStreams(authHeader)
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
                val response = apiService.getTopics(authHeader, channel.channelId)
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
                authHeader,
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
            val response = apiService.getSingleMessage(authHeader, messageId)
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
            apiService.addReaction(authHeader, messageId, emoji.name)
        } else {
            apiService.removeReaction(authHeader, messageId, emoji.name)
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
        val response = apiService.getUserPresence(authHeader, userId)
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
        usersResponseDto: AllUsersResponseDto,
        presencesResponseDto: AllPresencesResponseDto? = null,
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

        // "spinoza0@gmail.com"
        private const val CREDENTIALS_USERNAME = "ivan.sintyurin@gmail.com"

        // "Tu1s51Gtq1ec02fBd1lhAaOALD0hc2JH"
        private const val CREDENTIALS_PASSWORD = "RaINyfjtFHz8KEUFXtXzxPcVVRjaDdrm"

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