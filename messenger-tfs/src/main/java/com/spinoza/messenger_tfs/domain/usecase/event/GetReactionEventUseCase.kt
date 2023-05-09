package com.spinoza.messenger_tfs.domain.usecase.event

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.model.event.ReactionEvent
import com.spinoza.messenger_tfs.domain.repository.WebRepository
import javax.inject.Inject

class GetReactionEventUseCase @Inject constructor(private val repository: WebRepository) :
    EventUseCase<ReactionEvent> {

    override suspend operator fun invoke(
        queue: EventsQueue,
        messagesFilter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<ReactionEvent> {
        return repository.getReactionEvent(queue, messagesFilter, isLastMessageVisible)
    }
}