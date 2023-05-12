package com.spinoza.messenger_tfs.domain.usecase.event

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.model.event.UpdateMessageEvent
import com.spinoza.messenger_tfs.domain.repository.EventsRepository
import javax.inject.Inject

class GetUpdateMessageEventUseCase @Inject constructor(private val repository: EventsRepository) :
    EventUseCase<UpdateMessageEvent> {

    override suspend operator fun invoke(
        queue: EventsQueue,
        messagesFilter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<UpdateMessageEvent> {
        return repository.getUpdateMessageEvent(queue, messagesFilter, isLastMessageVisible)
    }
}