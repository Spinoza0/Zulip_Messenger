package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.cache.MessagesCache
import com.spinoza.messenger_tfs.data.database.MessengerDaoKeeper
import com.spinoza.messenger_tfs.data.network.ownuser.OwnUserKeeper
import com.spinoza.messenger_tfs.data.utils.dbToDomain
import com.spinoza.messenger_tfs.data.utils.runCatchingNonCancellation
import com.spinoza.messenger_tfs.data.utils.toDomain
import com.spinoza.messenger_tfs.di.DispatcherIO
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesResult
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.repository.DaoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DaoRepositoryImpl @Inject constructor(
    private val ownUserKeeper: OwnUserKeeper,
    private val messagesCache: MessagesCache,
    private val messengerDao: MessengerDaoKeeper,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) : DaoRepository {

    override suspend fun getStoredMessages(
        filter: MessagesFilter,
    ): Result<MessagesResult> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            messagesCache.reload()
            MessagesResult(
                messagesCache.getMessages(filter).toDomain(ownUserKeeper.value.userId),
                MessagePosition(MessagePosition.Type.LAST_POSITION)
            )
        }
    }

    override suspend fun getStoredChannels(channelsFilter: ChannelsFilter): Result<List<Channel>> =
        withContext(ioDispatcher) {
            runCatchingNonCancellation {
                val storedStreams = messengerDao.value.getStreams()
                storedStreams.dbToDomain(channelsFilter)
            }
        }

    override suspend fun getStoredTopics(channel: Channel): Result<List<Topic>> =
        withContext(ioDispatcher)
        {
            runCatchingNonCancellation {
                val storedTopics =
                    messengerDao.value.getTopics(channel.channelId, channel.isSubscribed)
                storedTopics.dbToDomain()
            }
        }
}