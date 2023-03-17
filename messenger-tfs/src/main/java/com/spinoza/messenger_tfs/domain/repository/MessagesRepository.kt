package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.Message

interface MessagesRepository {

    fun getUserId(): Long

    suspend fun getMessages(): RepositoryState

    suspend fun getAllChannels(): RepositoryState

    suspend fun getSubscribedChannels(): RepositoryState

    suspend fun getTopics(channelId: Long): RepositoryState

    suspend fun sendMessage(message: Message): RepositoryState

    suspend fun updateReaction(messageId: Long, reaction: String): RepositoryState
}