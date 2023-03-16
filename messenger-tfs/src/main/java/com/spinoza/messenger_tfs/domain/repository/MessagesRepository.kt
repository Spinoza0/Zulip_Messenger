package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.Message
import kotlinx.coroutines.flow.StateFlow

interface MessagesRepository {

    fun getState(userId: Long): StateFlow<RepositoryState>

    fun getAllChannels()

    fun getSubscribedChannels()

    suspend fun sendMessage(message: Message)

    suspend fun updateReaction(messageId: Long, userId: Long, reaction: String)
}