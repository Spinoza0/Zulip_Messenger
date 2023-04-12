package com.spinoza.messenger_tfs.presentation.utils

import com.cyberfox21.tinkofffintechseminar.di.GlobalDI
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EventsQueueProcessor(
    private val lifecycleScope: CoroutineScope,
    private val messagesFilter: MessagesFilter = MessagesFilter(),
) {

    var queue = EventsQueue()
    private val registerEventQueueUseCase = GlobalDI.INSTANCE.registerEventQueueUseCase
    private val deleteEventQueueUseCase = GlobalDI.INSTANCE.deleteEventQueueUseCase

    private var isQueueRegistered = false

    fun registerQueue(eventType: EventType, onSuccessCallback: (() -> Unit)? = null) {
        lifecycleScope.launch {
            if (isQueueRegistered) {
                deleteEventQueueUseCase(queue.queueId)
                isQueueRegistered = false
            }
            while (!isQueueRegistered) {
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