package com.spinoza.messenger_tfs.domain.usecase.event

import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.model.event.PresenceEvent
import com.spinoza.messenger_tfs.domain.repository.EventsRepository
import javax.inject.Inject

class GetPresenceEventsUseCase @Inject constructor(private val repository: EventsRepository) {

    suspend operator fun invoke(queue: EventsQueue): Result<List<PresenceEvent>> {
        return repository.getPresenceEvents(queue)
    }
}