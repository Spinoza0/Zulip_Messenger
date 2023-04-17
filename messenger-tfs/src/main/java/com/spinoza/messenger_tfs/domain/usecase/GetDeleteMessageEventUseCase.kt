package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.event.DeleteMessageEvent
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import javax.inject.Inject

class GetDeleteMessageEventUseCase @Inject constructor(private val repository: MessagesRepository) :
    EventUseCase<DeleteMessageEvent> {

    override suspend operator fun invoke(
        queue: EventsQueue,
        messagesFilter: MessagesFilter,
    ): Result<DeleteMessageEvent> {
        return repository.getDeleteMessageEvent(queue, messagesFilter)
    }
}