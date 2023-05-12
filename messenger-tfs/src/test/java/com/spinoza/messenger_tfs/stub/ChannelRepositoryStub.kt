package com.spinoza.messenger_tfs.stub

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.RepositoryError
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.repository.ChannelRepository
import com.spinoza.messenger_tfs.util.ERROR_MSG

class ChannelRepositoryStub : ChannelRepository {

    override suspend fun getChannelSubscriptionStatus(channelId: Long): Result<Boolean> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun createChannel(name: String, description: String): Result<Boolean> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun unsubscribeFromChannel(name: String): Result<Boolean> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun deleteChannel(channelId: Long): Result<Boolean> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getChannels(channelsFilter: ChannelsFilter): Result<List<Channel>> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getTopics(channel: Channel): Result<List<Topic>> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getTopic(filter: MessagesFilter): Result<Topic> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }
}