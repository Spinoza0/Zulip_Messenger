package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.ChannelFilter
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.User

interface MessagesRepository {

    fun getUser(): User

    suspend fun getMessages(channelFilter: ChannelFilter): RepositoryState

    suspend fun getAllChannels(): RepositoryState

    suspend fun getSubscribedChannels(): RepositoryState

    suspend fun getTopics(channelId: Long): RepositoryState

    suspend fun sendMessage(message: Message, channelFilter: ChannelFilter): RepositoryState

    suspend fun updateReaction(
        messageId: Long,
        reaction: String,
        channelFilter: ChannelFilter,
    ): RepositoryState
}