package com.spinoza.messenger_tfs.domain.usecase.event

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue

interface MessagesEventUseCase<T> {
    suspend operator fun invoke(
        queue: EventsQueue,
        messagesFilter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<T>
}