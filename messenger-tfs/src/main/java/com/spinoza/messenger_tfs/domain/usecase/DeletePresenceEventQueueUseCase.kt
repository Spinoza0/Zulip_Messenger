package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class DeletePresenceEventQueueUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(queueId: String) {
        return repository.deletePresenceEventQueue(queueId)
    }
}