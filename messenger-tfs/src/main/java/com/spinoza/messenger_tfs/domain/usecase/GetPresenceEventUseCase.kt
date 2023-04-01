package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.PresenceEvent
import com.spinoza.messenger_tfs.domain.repository.PresenceQueue
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

class GetPresenceEventUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(queue: PresenceQueue): RepositoryResult<PresenceEvent> {
        return repository.getPresenceEvent(queue)
    }
}