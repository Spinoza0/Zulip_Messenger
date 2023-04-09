package com.spinoza.messenger_tfs.presentation.viewmodel

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.usecase.DeleteEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.RegisterEventQueueUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EventsQueueProcessor(
    private val registerEventQueueUseCase: RegisterEventQueueUseCase,
    private val deleteEventQueueUseCase: DeleteEventQueueUseCase,
    private val messagesFilter: MessagesFilter = MessagesFilter(),
) {

    var queue = EventsQueue()

    private val scope = CoroutineScope(Dispatchers.IO)

    fun registerQueue(eventType: EventType, onSuccessCallback: () -> Unit) {
        scope.launch {
            var isRegistrationSuccess = false
            while (!isRegistrationSuccess) {
                registerEventQueueUseCase(listOf(eventType), messagesFilter).onSuccess {
                    queue = it
                    onSuccessCallback()
                    isRegistrationSuccess = true
                }
                delay(DELAY_BEFORE_REGISTRATION_ATTEMPT)
            }
        }
    }

    fun deleteQueue() {
        scope.launch {
            deleteEventQueueUseCase(queue.queueId)
        }
    }

    companion object {

        private const val DELAY_BEFORE_REGISTRATION_ATTEMPT = 10_000L
    }
}