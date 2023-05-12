package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.cache.MessagesCache
import com.spinoza.messenger_tfs.data.network.apiservice.ZulipApiService
import com.spinoza.messenger_tfs.data.network.apiservice.ZulipApiService.Companion.RESULT_SUCCESS
import com.spinoza.messenger_tfs.data.network.model.ApiKeyResponse
import com.spinoza.messenger_tfs.data.network.model.BasicResponse
import com.spinoza.messenger_tfs.data.network.model.WebLimitationsResponse
import com.spinoza.messenger_tfs.data.network.model.message.MessagesResponse
import com.spinoza.messenger_tfs.data.network.model.message.SendMessageResponse
import com.spinoza.messenger_tfs.data.network.model.message.SingleMessageResponse
import com.spinoza.messenger_tfs.data.network.model.presence.AllPresencesResponse
import com.spinoza.messenger_tfs.data.network.model.stream.TopicsResponse
import com.spinoza.messenger_tfs.data.network.model.user.AllUsersResponse
import com.spinoza.messenger_tfs.data.network.model.user.OwnUserResponse
import com.spinoza.messenger_tfs.data.network.model.user.UserResponse
import com.spinoza.messenger_tfs.data.utils.apiRequest
import com.spinoza.messenger_tfs.data.utils.createNarrowJsonForMessages
import com.spinoza.messenger_tfs.data.utils.isEqualTopicName
import com.spinoza.messenger_tfs.data.utils.runCatchingNonCancellation
import com.spinoza.messenger_tfs.data.utils.toDomain
import com.spinoza.messenger_tfs.data.utils.toDto
import com.spinoza.messenger_tfs.di.DispatcherIO
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesPageType
import com.spinoza.messenger_tfs.domain.model.MessagesResult
import com.spinoza.messenger_tfs.domain.model.RepositoryError
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.network.AuthorizationStorage
import com.spinoza.messenger_tfs.domain.network.WebLimitation
import com.spinoza.messenger_tfs.domain.repository.WebRepository
import com.spinoza.messenger_tfs.domain.util.EMPTY_STRING
import com.spinoza.messenger_tfs.domain.util.getCurrentTimestamp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class WebRepositoryImpl @Inject constructor(
    private val messagesCache: MessagesCache,
    private val apiService: ZulipApiService,
    private val authorizationStorage: AuthorizationStorage,
    private val webLimitation: WebLimitation,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) : WebRepository {

    override suspend fun logIn(email: String, password: String): Result<Boolean> =
        withContext(ioDispatcher) {
            authorizationStorage.makeAuthHeader(email)
            if (saveOwnUserData(email, password)) {
                updateWebLimitations()
                return@withContext Result.success(true)
            }
            runCatchingNonCancellation {
                val apiKeyResponse =
                    apiRequest<ApiKeyResponse> { apiService.fetchApiKey(email, password) }
                authorizationStorage.makeAuthHeader(apiKeyResponse.email, apiKeyResponse.apiKey)
                updateWebLimitations()
                saveOwnUserData(email, password, apiKeyResponse.apiKey)
            }
        }


    private suspend fun updateWebLimitations() {
        runCatchingNonCancellation {
            val response = apiRequest<WebLimitationsResponse> { apiService.getWebLimitations() }
            webLimitation.updateLimitations(
                response.maxStreamNameLength,
                response.maxStreamDescriptionLength,
                response.maxTopicLength,
                response.maxMessageLength,
                response.serverPresencePingIntervalSeconds,
                response.serverPresenceOfflineThresholdSeconds,
                response.messageContentEditLimitSeconds,
                response.topicEditingLimitSeconds,
                response.maxFileUploadSizeMib
            )
        }
    }

    private suspend fun saveOwnUserData(
        email: String,
        password: String,
        apiKey: String = EMPTY_STRING,
    ): Boolean {
        if (authorizationStorage.getAuthHeaderValue().isBlank()) return false
        runCatchingNonCancellation {
            apiRequest<OwnUserResponse> { apiService.getOwnUser() }
        }.onSuccess {
            authorizationStorage.saveData(it.userId, it.isAdmin, email, password, apiKey)
            return true
        }
        return false
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
                val narrow = filter.createNarrowJsonForMessages()
                when (messagesPageType) {
                    MessagesPageType.FIRST_UNREAD -> apiService.getMessages(
                        numBefore = ZulipApiService.HALF_MESSAGES_PACKET,
                        numAfter = ZulipApiService.HALF_MESSAGES_PACKET,
                        narrow = narrow,
                        anchor = ZulipApiService.ANCHOR_FIRST_UNREAD
                    )

                    MessagesPageType.NEWEST -> apiGetMessages(
                        numBefore = ZulipApiService.EMPTY_MESSAGES_PACKET,
                        numAfter = ZulipApiService.MAX_MESSAGES_PACKET,
                        narrow = narrow,
                        anchorId = messagesCache.getLastMessageId(filter),
                        ZulipApiService.ANCHOR_NEWEST
                    )

                    MessagesPageType.OLDEST -> apiGetMessages(
                        numBefore = ZulipApiService.MAX_MESSAGES_PACKET,
                        numAfter = ZulipApiService.EMPTY_MESSAGES_PACKET,
                        narrow = narrow,
                        anchorId = messagesCache.getFirstMessageId(filter),
                        ZulipApiService.ANCHOR_OLDEST
                    )

                    MessagesPageType.AFTER_STORED -> apiGetMessages(
                        numBefore = ZulipApiService.HALF_MESSAGES_PACKET,
                        numAfter = ZulipApiService.HALF_MESSAGES_PACKET,
                        narrow = narrow,
                        anchorId = messagesCache.getLastMessageId(filter),
                        ZulipApiService.ANCHOR_NEWEST
                    )

                    MessagesPageType.LAST, MessagesPageType.STORED -> apiService.getMessages(
                        numBefore = ZulipApiService.MAX_MESSAGES_PACKET,
                        numAfter = ZulipApiService.EMPTY_MESSAGES_PACKET,
                        narrow = narrow,
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
            MessagesResult(messagesCache.getMessages(filter), position)
        }
    }

    override suspend fun editMessage(
        messageId: Long,
        topic: String,
        content: String,
    ): Result<Long> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            if (topic.isBlank()) {
                apiRequest<BasicResponse> { apiService.editMessageContent(messageId, content) }
            } else {
                apiRequest<BasicResponse> { apiService.editMessageTopic(messageId, topic) }
            }
            messageId
        }
    }

    override suspend fun deleteMessage(messageId: Long): Result<Boolean> =
        withContext(ioDispatcher) {
            runCatchingNonCancellation {
                apiRequest<BasicResponse> { apiService.deleteMessage(messageId) }
                true
            }
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
        subject: String,
        content: String,
        filter: MessagesFilter,
    ): Result<Long> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val topicName = subject.ifBlank { filter.topic.name }
            val sendMessageResponse = apiRequest<SendMessageResponse> {
                apiService.sendMessageToStream(
                    filter.channel.channelId,
                    topicName,
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
                messagesCache.getMessages(filter),
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

    override suspend fun getMessageRawContent(
        messageId: Long,
        default: String,
    ): String = withContext(ioDispatcher) {
        var result = default
        runCatchingNonCancellation {
            val singleMessageResponse = apiRequest<SingleMessageResponse> {
                apiService.getSingleMessage(messageId, false)
            }
            result = singleMessageResponse.message.content
        }
        result
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

    private fun makeAllUsersAnswer(
        usersResponse: AllUsersResponse,
        presencesResponse: AllPresencesResponse? = null,
    ): List<User> {
        if (usersResponse.result != RESULT_SUCCESS) {
            throw RepositoryError(usersResponse.msg)
        }
        val users = mutableListOf<User>()
        val timestamp = getCurrentTimestamp()
        usersResponse.members
            .filter { it.isBot.not() && it.isActive }
            .forEach { userDto ->
                val presence =
                    if (presencesResponse != null && presencesResponse.result == RESULT_SUCCESS) {
                        presencesResponse.presences[userDto.email]?.let { presenceDto ->
                            if ((timestamp - presenceDto.aggregated.timestamp) <
                                webLimitation.getPresenceOfflineThresholdSeconds()
                            ) {
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
}