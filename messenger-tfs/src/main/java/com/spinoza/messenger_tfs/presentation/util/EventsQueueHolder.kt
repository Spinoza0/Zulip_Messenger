package com.spinoza.messenger_tfs.presentation.util

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.usecase.event.DeleteEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.RegisterEventQueueUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class EventsQueueHolder(
    private val lifecycleScope: CoroutineScope,
    private val registerEventQueueUseCase: RegisterEventQueueUseCase,
    private val deleteEventQueueUseCase: DeleteEventQueueUseCase,
) {

    var queue: EventsQueue = EventsQueue()

    @Volatile
    private var isQueueRegistered = false

    fun registerQueue(
        eventTypes: List<EventType>,
        onSuccessCallback: (() -> Unit)? = null,
        messagesFilter: MessagesFilter = MessagesFilter(),
    ) {
        lifecycleScope.launch {
            if (isQueueRegistered) {
                deleteQueue()
            }
            while (isActive && !isQueueRegistered) {
                registerEventQueueUseCase(eventTypes, messagesFilter).onSuccess {
                    queue = it
                    onSuccessCallback?.invoke()
                    isQueueRegistered = true
                }
                delay(DELAY_BEFORE_REGISTRATION_ATTEMPT)
            }
        }
    }

    suspend fun deleteQueue() {
        if (queue.queueId.isNotEmpty()) {
            deleteEventQueueUseCase(queue.queueId)
        }
        queue = EventsQueue()
        isQueueRegistered = false
    }

    companion object {

        private const val DELAY_BEFORE_REGISTRATION_ATTEMPT = 10_000L
    }
}