package com.spinoza.messenger_tfs.domain.usecase.event

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.repository.EventsRepository
import javax.inject.Inject

class RegisterEventQueueUseCase @Inject constructor(private val repository: EventsRepository) {

    suspend operator fun invoke(
        eventTypes: List<EventType>,
        messagesFilter: MessagesFilter = MessagesFilter(),
    ): Result<EventsQueue> {
        return repository.registerEventQueue(eventTypes, messagesFilter)
    }
}