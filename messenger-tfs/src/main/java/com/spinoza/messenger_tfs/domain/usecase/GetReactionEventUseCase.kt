package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.model.event.ReactionEvent
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class GetReactionEventUseCase(private val repository: MessagesRepository) :
    EventUseCase<ReactionEvent> {

    override suspend operator fun invoke(
        queue: EventsQueue,
        messagesFilter: MessagesFilter,
    ): Result<ReactionEvent> {
        return repository.getReactionEvent(queue, messagesFilter)
    }
}