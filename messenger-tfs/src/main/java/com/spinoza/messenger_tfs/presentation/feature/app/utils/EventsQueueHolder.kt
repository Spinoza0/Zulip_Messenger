package com.spinoza.messenger_tfs.presentation.feature.app.utils

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.usecase.DeleteEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.RegisterEventQueueUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class EventsQueueHolder(
    private val lifecycleScope: CoroutineScope,
    private val registerEventQueueUseCase: RegisterEventQueueUseCase,
    private val deleteEventQueueUseCase: DeleteEventQueueUseCase,
    private val messagesFilter: MessagesFilter = MessagesFilter(),
) {

    constructor(eventsQueueHolder: EventsQueueHolder) : this(
        eventsQueueHolder.lifecycleScope,
        eventsQueueHolder.registerEventQueueUseCase,
        eventsQueueHolder.deleteEventQueueUseCase,
        eventsQueueHolder.messagesFilter
    )

    var queue: EventsQueue = EventsQueue()
    private var isQueueRegistered = false

    fun registerQueue(eventType: EventType, onSuccessCallback: (() -> Unit)? = null) {
        lifecycleScope.launch {
            if (isQueueRegistered) {
                deleteEventQueueUseCase(queue.queueId)
                isQueueRegistered = false
            }
            while (isActive && !isQueueRegistered) {
                registerEventQueueUseCase(listOf(eventType), messagesFilter).onSuccess {
                    queue = it
                    onSuccessCallback?.invoke()
                    isQueueRegistered = true
                }
                delay(DELAY_BEFORE_REGISTRATION_ATTEMPT)
            }
        }
    }

    fun deleteQueue() {
        lifecycleScope.launch {
            deleteEventQueueUseCase(queue.queueId)
        }
    }

    companion object {

        private const val DELAY_BEFORE_REGISTRATION_ATTEMPT = 10_000L
    }
}