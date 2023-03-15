package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.RepositoryState
import kotlinx.coroutines.flow.SharedFlow

interface MessagesRepository {

    fun getState(userId: Int): SharedFlow<RepositoryState>

    suspend fun sendMessage(message: Message)

    suspend fun updateReaction(messageId: Int, userId: Int, reaction: String)
}