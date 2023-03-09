package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class UpdateMessageUseCase(private val repository: MessagesRepository) {
    suspend operator fun invoke(message: Message) {
        repository.updateMessage(message)
    }
}