package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.BuildConfig
import com.spinoza.messenger_tfs.data.database.MessengerDao
import com.spinoza.messenger_tfs.data.network.apiservice.ZulipApiService
import com.spinoza.messenger_tfs.data.network.model.BasicResponse
import com.spinoza.messenger_tfs.data.network.model.message.MessagesResponse
import com.spinoza.messenger_tfs.data.network.model.stream.StreamSubscriptionStatusResponse
import com.spinoza.messenger_tfs.data.network.model.stream.SubscriptionItemDto
import com.spinoza.messenger_tfs.data.network.model.stream.TopicsResponse
import com.spinoza.messenger_tfs.data.utils.apiRequest
import com.spinoza.messenger_tfs.data.utils.createNarrowJsonForMessages
import com.spinoza.messenger_tfs.data.utils.dtoToDomain
import com.spinoza.messenger_tfs.data.utils.runCatchingNonCancellation
import com.spinoza.messenger_tfs.data.utils.toDbModel
import com.spinoza.messenger_tfs.di.DispatcherIO
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.network.AuthorizationStorage
import com.spinoza.messenger_tfs.domain.repository.ChannelRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ChannelRepositoryImpl @Inject constructor(
    private val messengerDao: MessengerDao,
    private val apiService: ZulipApiService,
    private val authorizationStorage: AuthorizationStorage,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) : ChannelRepository {

    override suspend fun createChannel(name: String, description: String): Result<Boolean> =
        withContext(ioDispatcher) {
            runCatchingNonCancellation {
                val subscriptions = listOf(SubscriptionItemDto(name, description))
                val param = Json.encodeToString(subscriptions)
                apiRequest<BasicResponse> { apiService.subscribeToStream(param) }
                true
            }
        }

    override suspend fun unsubscribeFromChannel(name: String): Result<Boolean> =
        withContext(ioDispatcher) {
            runCatchingNonCancellation {
                val subscriptions = Json.encodeToString(listOf(name))
                val principals = Json.encodeToString(listOf(authorizationStorage.getUserId()))
                apiRequest<BasicResponse> {
                    apiService.unsubscribeFromStream(subscriptions, principals)
                }
                true
            }
        }

    override suspend fun deleteChannel(channelId: Long): Result<Boolean> =
        withContext(ioDispatcher) {
            runCatchingNonCancellation {
                apiRequest<BasicResponse> { apiService.deleteStream(channelId) }
                true
            }
        }

    override suspend fun getChannelSubscriptionStatus(channelId: Long): Result<Boolean> =
        withContext(ioDispatcher) {
            runCatchingNonCancellation {
                val response = apiRequest<StreamSubscriptionStatusResponse> {
                    apiService.getStreamSubscriptionStatus(
                        authorizationStorage.getUserId(),
                        channelId
                    )
                }
                response.isSubscribed
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

    companion object {

        private const val GET_TOPIC_IGNORE_PREVIOUS_MESSAGES = 0
        private const val GET_TOPIC_MAX_UNREAD_MESSAGES_COUNT =
            BuildConfig.GET_TOPIC_MAX_UNREAD_MESSAGES_COUNT
    }
}