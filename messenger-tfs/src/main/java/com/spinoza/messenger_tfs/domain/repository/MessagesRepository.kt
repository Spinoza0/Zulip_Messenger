package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.*

interface MessagesRepository {

    fun getCurrentUser(): Pair<RepositoryResult, User?>

    suspend fun getUser(userId: Long): Pair<RepositoryResult, User?>

    suspend fun getAllUsers(): Pair<RepositoryResult, List<User>>

    suspend fun getMessages(messagesFilter: MessagesFilter): Pair<RepositoryResult, MessagesResult?>

    suspend fun getAllChannels(): Pair<RepositoryResult, List<Channel>>

    suspend fun getSubscribedChannels(): Pair<RepositoryResult, List<Channel>>

    suspend fun getTopics(channelId: Long): Pair<RepositoryResult, List<Topic>>

    suspend fun sendMessage(
        message: Message,
        messagesFilter: MessagesFilter,
    ): Pair<RepositoryResult, MessagesResult?>

    suspend fun updateReaction(
        messageId: Long,
        reaction: String,
        messagesFilter: MessagesFilter,
    ): Pair<RepositoryResult, MessagesResult?>
}