package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.Topic

interface ChannelRepository {

    suspend fun getChannelSubscriptionStatus(channelId: Long): Result<Boolean>

    suspend fun createChannel(name: String, description: String): Result<Boolean>

    suspend fun unsubscribeFromChannel(name: String): Result<Boolean>

    suspend fun deleteChannel(channelId: Long): Result<Boolean>

    suspend fun getChannels(channelsFilter: ChannelsFilter): Result<List<Channel>>

    suspend fun getTopics(channel: Channel): Result<List<Topic>>

    suspend fun getTopic(filter: MessagesFilter): Result<Topic>
}