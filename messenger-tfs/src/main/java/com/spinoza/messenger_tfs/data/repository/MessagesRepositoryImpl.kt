package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.*
import com.spinoza.messenger_tfs.data.model.*
import com.spinoza.messenger_tfs.data.network.ZulipApiFactory
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.repository.MessagePosition
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.MessagesResult
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import java.util.*


// TODO: 1) отрефакторить - не все сообщения сразу отправлять, а только новые или измененные
// TODO: 2) отрефакторить - кэш (messagesLocalCache) вынести в отдельный класс

class MessagesRepositoryImpl private constructor() : MessagesRepository {

    private var ownUser = UserDto()

    private val authHeader =
        Credentials.basic("spinoza0@gmail.com", "Tu1s51Gtq1ec02fBd1lhAaOALD0hc2JH")
    private val messagesLocalCache = TreeSet<MessageDto>()
    private val apiService = ZulipApiFactory.apiService

    init {
        // TODO: for testing purpose
        messagesLocalCache.addAll(prepareTestData())
    }

    override suspend fun getOwnUser(): RepositoryResult<User> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getOwnUser(authHeader)
            if (response.isSuccessful) {
                response.body()?.let { ownResponseDto: OwnResponseDto ->
                    if (ownResponseDto.result == RESULT_SUCCESS) {
                        ownUser = ownResponseDto.toUserDto()
                        val presence = getUserPresence(ownUser.userId)
                        RepositoryResult.Success(ownUser.toDomain(presence))
                    } else {
                        RepositoryResult.Failure.OwnUserNotFound(ownResponseDto.msg)
                    }
                } ?: RepositoryResult.Failure.OwnUserNotFound(response.message())
            } else {
                RepositoryResult.Failure.OwnUserNotFound(response.message())
            }
        }.getOrElse {
            RepositoryResult.Failure.Network(getErrorText(it))
        }
    }

    override suspend fun getUser(userId: Long): RepositoryResult<User> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.getUser(authHeader, userId)
                if (response.isSuccessful) {
                    response.body()?.let { userResponseDto ->
                        if (userResponseDto.result == RESULT_SUCCESS) {
                            val presence = getUserPresence(userResponseDto.user.userId)
                            RepositoryResult.Success(userResponseDto.user.toDomain(presence))
                        } else {
                            RepositoryResult.Failure.UserNotFound(userId, userResponseDto.msg)
                        }
                    } ?: RepositoryResult.Failure.UserNotFound(userId, response.message())
                } else {
                    RepositoryResult.Failure.UserNotFound(userId, response.message())
                }
            }.getOrElse {
                it.printStackTrace()
                RepositoryResult.Failure.Network(getErrorText(it))
            }
        }

    override suspend fun getUsersByFilter(usersFilter: String): RepositoryResult<List<User>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val usersResponse = apiService.getAllUsers(authHeader)
                if (usersResponse.isSuccessful) {
                    usersResponse.body()?.let { allUsersResponseDto ->
                        if (allUsersResponseDto.result == RESULT_SUCCESS) {
                            val presencesResponse = apiService.getAllPresences(authHeader)
                            if (presencesResponse.isSuccessful) {
                                makeAllUsersAnswer(
                                    usersFilter,
                                    allUsersResponseDto,
                                    presencesResponse.body()
                                )
                            } else {
                                makeAllUsersAnswer(usersFilter, allUsersResponseDto)
                            }
                        } else {
                            RepositoryResult.Failure.LoadingUsers(allUsersResponseDto.msg)
                        }
                    } ?: RepositoryResult.Failure.LoadingUsers(usersResponse.message())
                } else {
                    RepositoryResult.Failure.LoadingUsers(usersResponse.message())
                }
            }.getOrElse {
                RepositoryResult.Failure.Network(getErrorText(it))
            }
        }

    override suspend fun getMessages(
        messagesFilter: MessagesFilter,
    ): RepositoryResult<MessagesResult> = withContext(Dispatchers.IO) {
        // TODO: for testing purpose
        delay(DELAY_VALUE)
        if (!isErrorInRepository()) {
            RepositoryResult.Success(
                MessagesResult(
                    messagesLocalCache.toDomain(ownUser.userId, messagesFilter),
                    MessagePosition()
                )
            )
        } else {
            RepositoryResult.Failure.LoadingMessages(messagesFilter)
        }
    }

    override suspend fun getChannels(
        channelsFilter: ChannelsFilter,
    ): RepositoryResult<List<Channel>> = withContext(Dispatchers.IO) {
        runCatching {
            if (channelsFilter.isSubscribed) {
                val response = apiService.getSubscribedStreams(authHeader)
                if (response.isSuccessful) {
                    response.body()?.let { subscribedStreamsDto ->
                        if (subscribedStreamsDto.result == RESULT_SUCCESS) {
                            RepositoryResult.Success(
                                subscribedStreamsDto.subscriptions.toDomain(channelsFilter)
                            )
                        } else {
                            RepositoryResult.Failure.LoadingChannels(
                                channelsFilter,
                                subscribedStreamsDto.msg
                            )
                        }
                    } ?: RepositoryResult.Failure.LoadingChannels(
                        channelsFilter,
                        response.message()
                    )
                } else {
                    RepositoryResult.Failure.LoadingChannels(channelsFilter, response.message())
                }
            } else {
                val response = apiService.getAllStreams(authHeader)
                if (response.isSuccessful) {
                    response.body()?.let { allStreamsDto ->
                        if (allStreamsDto.result == RESULT_SUCCESS) {
                            RepositoryResult.Success(
                                allStreamsDto.streams.toDomain(channelsFilter)
                            )
                        } else {
                            RepositoryResult.Failure.LoadingChannels(
                                channelsFilter,
                                allStreamsDto.msg
                            )
                        }
                    } ?: RepositoryResult.Failure.LoadingChannels(
                        channelsFilter,
                        response.message()
                    )
                } else {
                    RepositoryResult.Failure.LoadingChannels(channelsFilter, response.message())
                }
            }
        }.getOrElse {
            RepositoryResult.Failure.LoadingChannels(channelsFilter, getErrorText(it))
        }
    }

    override suspend fun getTopics(channel: Channel): RepositoryResult<List<Topic>> =
        withContext(Dispatchers.IO) {
            // TODO: for testing purpose
            delay(DELAY_VALUE)
            val topics = channelsDto
                .find { it.id == channel.channelId }
                ?.topics
                ?.toDomain(messagesLocalCache, channel.channelId) ?: listOf()
            if (!isErrorInRepository()) {
                RepositoryResult.Success(topics)
            } else {
                RepositoryResult.Failure.LoadingChannelTopics(channel)
            }
        }

    override suspend fun getTopic(
        messagesFilter: MessagesFilter,
    ): RepositoryResult<Topic> = withContext(Dispatchers.IO) {
        if (!isErrorInRepository()) {
            RepositoryResult.Success(
                TopicDto(messagesFilter.topic.name, Message.UNDEFINED_ID)
                    .toDomain(messagesLocalCache, messagesFilter.channel.channelId)
            )
        } else {
            RepositoryResult.Failure.LoadingTopicData(messagesFilter)
        }
    }

    override suspend fun sendMessage(
        message: Message,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<MessagesResult> = withContext(Dispatchers.IO) {
        // TODO: for testing purpose
        delay(DELAY_VALUE)
        val newMessageId = if (message.id == Message.UNDEFINED_ID) {
            messagesLocalCache.size.toLong()
        } else {
            message.id
        }
        messagesLocalCache.add(
            message.toDto(
                userId = message.user.userId,
                messageId = newMessageId,
                messagesFilter = messagesFilter
            )
        )
        if (!isErrorInRepository()) {
            RepositoryResult.Success(
                MessagesResult(
                    messagesLocalCache.toDomain(message.user.userId, messagesFilter),
                    MessagePosition(type = MessagePosition.Type.LAST_POSITION)
                )
            )
        } else {
            RepositoryResult.Failure.SendingMessage(errorText)
        }
    }

    override suspend fun updateReaction(
        messageId: Long,
        reaction: String,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<MessagesResult> = withContext(Dispatchers.IO) {
        // TODO: for testing purpose
        delay(DELAY_VALUE)
        val messageDto = messagesLocalCache.find { it.id == messageId }
        if (messageDto == null) {
            RepositoryResult.Failure.MessageNotFound(messageId)
        } else {
            val reactionDto = messageDto.reactions[reaction]
            val newReactionsDto = messageDto.reactions.toMutableMap()
            if (reactionDto != null) {
                val newUsersIds =
                    reactionDto.usersIds.removeIfExistsOrAddToList(ownUser.userId)
                if (newUsersIds.isNotEmpty()) {
                    newReactionsDto[reaction] = ReactionParamDto(newUsersIds)
                } else {
                    newReactionsDto.remove(reaction)
                }
            } else {
                newReactionsDto[reaction] = ReactionParamDto(listOf(ownUser.userId))
            }
            messagesLocalCache.removeIf { it.id == messageId }
            messagesLocalCache.add(messageDto.copy(reactions = newReactionsDto))
            if (!isErrorInRepository()) {
                RepositoryResult.Success(
                    MessagesResult(
                        messagesLocalCache.toDomain(ownUser.userId, messagesFilter),
                        MessagePosition(
                            type = MessagePosition.Type.EXACTLY,
                            messageId = messageId
                        )
                    )
                )
            } else {
                RepositoryResult.Failure.UpdatingReaction(errorText)
            }
        }
    }

    private fun List<Long>.removeIfExistsOrAddToList(value: Long): List<Long> {
        val result = mutableListOf<Long>()
        var deletedFromList = false
        this.forEach { existingValue ->
            if (existingValue == value) {
                deletedFromList = true
            } else {
                result.add(existingValue)
            }
        }
        if (!deletedFromList) {
            result.add(value)
        }
        return result
    }

    private suspend fun getUserPresence(userId: Long): User.Presence = runCatching {
        val response = apiService.getUserPresence(authHeader, userId)
        if (response.isSuccessful) {
            response.body()?.let { presenceResponseDto ->
                if (presenceResponseDto.result == RESULT_SUCCESS) {
                    presenceResponseDto.presence.toDomain()
                } else {
                    User.Presence.OFFLINE
                }
            } ?: User.Presence.OFFLINE
        } else {
            User.Presence.OFFLINE
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
        RepositoryResult.Success(if (usersFilter.isBlank())
            users
        else
            users.filter { it.full_name.contains(usersFilter, true) }
        )
    } else {
        RepositoryResult.Failure.LoadingUsers(usersResponseDto.msg)
    }

    private fun getErrorText(e: Throwable): String =
        e.localizedMessage ?: e.message ?: e.toString()

    companion object {

        // TODO: for testing purpose
        private const val DELAY_VALUE = 1000L

        private const val RESULT_SUCCESS = "success"

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