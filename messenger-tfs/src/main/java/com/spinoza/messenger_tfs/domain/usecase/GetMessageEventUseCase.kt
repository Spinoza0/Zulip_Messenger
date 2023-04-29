package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.model.event.MessageEvent
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import javax.inject.Inject

class GetMessageEventUseCase @Inject constructor(private val repository: MessagesRepository) :
    EventUseCase<MessageEvent> {

    override suspend operator fun invoke(
        queue: EventsQueue,
        messagesFilter: MessagesFilter,
        isLastMessageVisible: Boolean
    ): Result<MessageEvent> {
        return repository.getMessageEvent(queue, messagesFilter, isLastMessageVisible)
    }
}