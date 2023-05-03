package com.spinoza.messenger_tfs.stub

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesResult
import com.spinoza.messenger_tfs.domain.model.RepositoryError
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.repository.DaoRepository
import com.spinoza.messenger_tfs.util.createChannels

class DaoRepositoryStub : DaoRepository {

    override suspend fun getStoredMessages(filter: MessagesFilter): Result<MessagesResult> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getStoredChannels(channelsFilter: ChannelsFilter): Result<List<Channel>> {
        return createChannels(channelsFilter)
    }

    override suspend fun getStoredTopics(channel: Channel): Result<List<Topic>> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    private companion object {

        const val ERROR_MSG = "Repository error"
    }
}