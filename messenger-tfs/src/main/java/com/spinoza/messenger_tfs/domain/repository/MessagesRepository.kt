package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.MessageEntity
import com.spinoza.messenger_tfs.domain.model.RepositoryState

interface MessagesRepository {

    fun getState(): RepositoryState

    suspend fun loadMessages()

    suspend fun sendMessage(messageEntity: MessageEntity)

    suspend fun updateMessage(messageEntity: MessageEntity)
}