package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

class RegisterEventQueueUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(eventType: EventType): RepositoryResult<EventsQueue> {
        return repository.registerEventQueue(eventType)
    }
}