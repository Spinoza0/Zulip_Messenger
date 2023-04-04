package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.event.DeleteMessageEvent
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

class GetDeleteMessageEventUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(
        queue: EventsQueue,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<DeleteMessageEvent> {
        return repository.getDeleteMessageEvent(queue, messagesFilter)
    }
}