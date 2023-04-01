package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.PresenceQueue
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

class RegisterPresenceEventQueueUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(): RepositoryResult<PresenceQueue> {
        return repository.registerPresenceEventQueue()
    }
}