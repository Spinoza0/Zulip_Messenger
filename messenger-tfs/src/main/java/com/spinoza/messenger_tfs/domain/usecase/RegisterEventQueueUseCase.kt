package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class RegisterEventQueueUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(
        eventTypes: List<EventType>,
        messagesFilter: MessagesFilter = MessagesFilter(),
    ): Result<EventsQueue> {
        return repository.registerEventQueue(eventTypes, messagesFilter)
    }
}