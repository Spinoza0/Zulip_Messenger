package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.*

interface MessagesRepository {

    suspend fun getCurrentUser(): RepositoryResult<User>

    suspend fun getUser(userId: Long): RepositoryResult<User>

    suspend fun getUsersByFilter(usersFilter: String): RepositoryResult<List<User>>

    suspend fun getMessages(messagesFilter: MessagesFilter): RepositoryResult<MessagesResult>

    suspend fun getChannels(channelsFilter: ChannelsFilter): RepositoryResult<List<Channel>>

    suspend fun getTopics(channel: Channel): RepositoryResult<List<Topic>>

    suspend fun getTopic(messagesFilter: MessagesFilter): RepositoryResult<Topic>

    suspend fun sendMessage(
        message: Message,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<MessagesResult>

    suspend fun updateReaction(
        messageId: Long,
        reaction: String,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<MessagesResult>
}