package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.RepositoryState

interface MessagesRepository {

    fun getState(): RepositoryState

    suspend fun loadMessages()

    suspend fun addMessage(message: Message)

    suspend fun updateMessage(message: Message)
}