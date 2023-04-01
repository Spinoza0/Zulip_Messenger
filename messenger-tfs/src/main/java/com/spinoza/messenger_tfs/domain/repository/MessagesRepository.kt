package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.*

interface MessagesRepository {

    suspend fun getOwnUserId(): RepositoryResult<Long>

    suspend fun getOwnUser(): RepositoryResult<User>

    suspend fun getUser(userId: Long): RepositoryResult<User>

    suspend fun getUsersByFilter(usersFilter: String): RepositoryResult<List<User>>

    suspend fun getMessages(
        messagesFilter: MessagesFilter,
        messageId: Long = Message.UNDEFINED_ID,
    ): RepositoryResult<MessagesResult>

    suspend fun getChannels(channelsFilter: ChannelsFilter): RepositoryResult<List<Channel>>

    suspend fun getTopics(channel: Channel): RepositoryResult<List<Topic>>

    suspend fun getTopic(messagesFilter: MessagesFilter): RepositoryResult<Topic>

    suspend fun sendMessage(
        content: String,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<MessagesResult>

    suspend fun updateReaction(
        messageId: Long,
        emoji: Emoji,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<MessagesResult>

    suspend fun registerPresenceEventQueue(): RepositoryResult<PresenceQueue>

    suspend fun getPresenceEvent(queue: PresenceQueue): RepositoryResult<PresenceEvent>

    suspend fun deletePresenceEventQueue(queueId: String)
}