package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.event.ChannelEvent
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class GetChannelEventsUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(queue: EventsQueue): Result<List<ChannelEvent>> {
        return repository.getChannelEvents(queue)
    }
}