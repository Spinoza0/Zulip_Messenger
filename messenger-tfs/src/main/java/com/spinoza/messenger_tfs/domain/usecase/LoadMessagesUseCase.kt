package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class LoadMessagesUseCase(private val repository: MessagesRepository) {
    suspend operator fun invoke() {
        repository.loadMessages()
    }
}