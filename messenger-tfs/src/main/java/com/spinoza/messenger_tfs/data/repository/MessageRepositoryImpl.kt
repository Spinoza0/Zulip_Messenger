package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.cache.MessagesCache
import com.spinoza.messenger_tfs.data.network.apiservice.ZulipApiService
import com.spinoza.messenger_tfs.data.network.model.BasicResponse
import com.spinoza.messenger_tfs.data.network.model.message.MessagesResponse
import com.spinoza.messenger_tfs.data.network.model.message.SendMessageResponse
import com.spinoza.messenger_tfs.data.network.model.message.SingleMessageResponse
import com.spinoza.messenger_tfs.data.network.model.stream.TopicsResponse
import com.spinoza.messenger_tfs.data.utils.apiRequest
import com.spinoza.messenger_tfs.data.utils.createNarrowJsonForMessages
import com.spinoza.messenger_tfs.data.utils.isEqualTopicName
import com.spinoza.messenger_tfs.data.utils.runCatchingNonCancellation
import com.spinoza.messenger_tfs.data.utils.toDto
import com.spinoza.messenger_tfs.di.DispatcherIO
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesPageType
import com.spinoza.messenger_tfs.domain.model.MessagesResult
import com.spinoza.messenger_tfs.domain.network.AuthorizationStorage
import com.spinoza.messenger_tfs.domain.repository.MessageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messagesCache: MessagesCache,
    private val apiService: ZulipApiService,
    private val authorizationStorage: AuthorizationStorage,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) : MessageRepository {

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
            messagesCache.addAll(messagesResponse.messages, messagesPageType, filter)
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
}