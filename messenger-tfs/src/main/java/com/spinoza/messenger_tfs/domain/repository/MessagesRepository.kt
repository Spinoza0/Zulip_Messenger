package com.spinoza.messenger_tfs.domain.repository

import androidx.lifecycle.LiveData
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.RepositoryState

interface MessagesRepository {

    fun getState(): LiveData<RepositoryState>

    suspend fun loadMessages()

    suspend fun sendMessage(message: Message)

    suspend fun updateMessage(message: Message)
}