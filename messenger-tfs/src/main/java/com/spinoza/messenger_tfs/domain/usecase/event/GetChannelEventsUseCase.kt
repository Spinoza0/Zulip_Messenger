package com.spinoza.messenger_tfs.domain.usecase.event

import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.event.ChannelEvent
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.repository.MessengerRepository
import javax.inject.Inject

class GetChannelEventsUseCase @Inject constructor(private val repository: MessengerRepository) {

    suspend operator fun invoke(
        queue: EventsQueue,
        channelsFilter: ChannelsFilter,
    ): Result<List<ChannelEvent>> {
        return repository.getChannelEvents(queue, channelsFilter)
    }
}