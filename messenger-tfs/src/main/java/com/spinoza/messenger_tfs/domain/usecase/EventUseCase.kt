package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue

interface EventUseCase<T> {
    suspend operator fun invoke(
        queue: EventsQueue,
        messagesFilter: MessagesFilter,
    ): Result<T>
}