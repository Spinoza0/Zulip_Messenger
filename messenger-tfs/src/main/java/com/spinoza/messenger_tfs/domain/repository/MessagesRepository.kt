package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.Message

interface MessagesRepository {

    fun getUserId(): Long

    fun getMessages(): RepositoryState

    fun getAllChannels(): RepositoryState

    fun getSubscribedChannels(): RepositoryState

    suspend fun sendMessage(message: Message): RepositoryState

    suspend fun updateReaction(messageId: Long, reaction: String): RepositoryState
}