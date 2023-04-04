package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.model.event.MessagesEvent
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

class GetMessageEventsUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(
        queue: EventsQueue,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<MessagesEvent> {
        return repository.getMessageEvents(queue, messagesFilter)
    }
}