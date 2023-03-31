package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.model.message.NarrowItemDto
import com.spinoza.messenger_tfs.data.model.message.ReactionDto
import com.spinoza.messenger_tfs.data.model.presence.AllPresencesResponseDto
import com.spinoza.messenger_tfs.data.model.user.AllUsersResponseDto
import com.spinoza.messenger_tfs.data.model.user.OwnResponseDto
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


// TODO: 1) отрефакторить - не все сообщения сразу отправлять, а только новые или измененные
// TODO: 2) отрефакторить - кэш (messagesLocalCache) вынести в отдельный класс

class MessagesRepositoryImpl private constructor() : MessagesRepository {

    private var ownUser = UserDto()
    private val authHeader = Credentials.basic(CREDENTIALS_USERNAME, CREDENTIALS_PASSWORD)
    private val apiService = ZulipApiFactory.apiService

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
        messageId: Long,
    ): RepositoryResult<MessagesResult> = withContext(Dispatchers.IO) {
        runCatching {
            val narrowDtoList = mutableListOf(
                NarrowItemDto(OPERATOR_STREAM, messagesFilter.channel.name)
            )
            if (messagesFilter.topic.name.isNotEmpty()) {
                narrowDtoList.add(NarrowItemDto(OPERATOR_TOPIC, messagesFilter.topic.name))
            }
            val narrow = Json.encodeToString(narrowDtoList)
            val response = apiService.getMessages(
                authHeader = authHeader,
                narrow = narrow
            )
            if (response.isSuccessful) {
                response.body()?.let { messagesResponseDto ->
                    if (messagesResponseDto.result == RESULT_SUCCESS) {
                        val positionType =
                            if (messageId != Message.UNDEFINED_ID) {
                                if (messagesResponseDto.messages.last().id == messageId) {
                                    MessagePosition.Type.LAST_POSITION
                                }
                                else {
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
                    } else {
                        RepositoryResult.Failure.LoadingMessages(
                            messagesFilter,
                            messagesResponseDto.msg
                        )
                    }
                } ?: RepositoryResult.Failure.LoadingMessages(messagesFilter, response.message())
            } else {
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
            RepositoryResult.Failure.Network(getErrorText(it))
        }
    }

    override suspend fun getTopics(channel: Channel): RepositoryResult<List<Topic>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.getTopics(authHeader, channel.channelId)
                if (response.isSuccessful) {
                    response.body()?.let { topicsResponseDto ->
                        if (topicsResponseDto.result == RESULT_SUCCESS) {
                            RepositoryResult.Success(
                                topicsResponseDto.topics.toDomain(
                                    getMessages(MessagesFilter(channel, Topic("", 0)))
                                )
                            )
                        } else {
                            RepositoryResult.Failure.LoadingChannelTopics(
                                channel,
                                topicsResponseDto.msg
                            )
                        }
                    } ?: RepositoryResult.Failure.LoadingChannelTopics(channel, response.message())
                } else {
                    RepositoryResult.Failure.LoadingChannelTopics(channel, response.message())
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
                // TODO: add using https://zulip.com/api/render-message#render-message
                content
            )
            if (response.isSuccessful && response.body() != null) {
                if (response.body()?.result == RESULT_SUCCESS) {
                    val messageId = response.body()?.messageId ?: Message.UNDEFINED_ID
                    getMessages(messagesFilter, messageId)
                } else {
                    RepositoryResult.Failure.SendingMessage(response.body()?.msg ?: "")
                }
            } else {
                RepositoryResult.Failure.SendingMessage(response.message())
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
            if (response.isSuccessful) {
                response.body()?.let { singleMessageResponseDto ->
                    if (singleMessageResponseDto.result == RESULT_SUCCESS) {
                        updateReaction(
                            singleMessageResponseDto.message.reactions,
                            messageId,
                            emoji,
                            messagesFilter
                        )
                    } else {
                        RepositoryResult.Failure.UpdatingReaction(singleMessageResponseDto.msg)
                    }
                } ?: RepositoryResult.Failure.UpdatingReaction(response.message())
            } else {
                RepositoryResult.Failure.UpdatingReaction(response.message())
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
            it.emoji_code == emoji.code && it.emoji_name == emoji.name
        }
        val response = if (isAddReaction) {
            apiService.addReaction(authHeader, messageId, emoji.name, emoji.code)
        } else {
            apiService.removeReaction(authHeader, messageId, emoji.name, emoji.code)
        }
        if (response.isSuccessful && response.body() != null) {
            if (response.body()?.result == RESULT_SUCCESS) {
                getMessages(messagesFilter, messageId)
            } else {
                RepositoryResult.Failure.UpdatingReaction(response.body()?.msg ?: "")
            }
        } else {
            RepositoryResult.Failure.UpdatingReaction(response.message())
        }
    }.getOrElse {
        it.printStackTrace()
        RepositoryResult.Failure.UpdatingReaction(getErrorText(it))
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
            users.filter { it.fullName.contains(usersFilter, true) }
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