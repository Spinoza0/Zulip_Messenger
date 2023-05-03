package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesResult
import com.spinoza.messenger_tfs.domain.model.Topic

interface DaoRepository {

    suspend fun getStoredMessages(filter: MessagesFilter): Result<MessagesResult>

    suspend fun getStoredChannels(channelsFilter: ChannelsFilter): Result<List<Channel>>

    suspend fun getStoredTopics(channel: Channel): Result<List<Topic>>
}