package com.spinoza.messenger_tfs.domain.usecase.event

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.event.DeleteMessageEvent
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.repository.EventsRepository
import javax.inject.Inject

class GetDeleteMessageEventUseCase @Inject constructor(private val repository: EventsRepository) :
    MessagesEventUseCase<DeleteMessageEvent> {

    override suspend operator fun invoke(
        queue: EventsQueue,
        messagesFilter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<DeleteMessageEvent> {
        return repository.getDeleteMessageEvent(queue, messagesFilter, isLastMessageVisible)
    }
}