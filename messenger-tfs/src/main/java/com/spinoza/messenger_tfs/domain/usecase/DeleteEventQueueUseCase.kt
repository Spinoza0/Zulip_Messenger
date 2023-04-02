package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class DeleteEventQueueUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(queueId: String) {
        return repository.deleteEventQueue(queueId)
    }
}