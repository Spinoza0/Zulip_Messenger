package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagesState
import kotlinx.coroutines.flow.SharedFlow

interface MessagesRepository {

    fun getMessagesState(): SharedFlow<MessagesState>

    suspend fun loadMessages(userId: Int)

    suspend fun sendMessage(message: Message)

    suspend fun updateReaction(messageId: Int, userId: Int, reaction: String)
}