package com.spinoza.messenger_tfs.domain.usecase.event

import com.spinoza.messenger_tfs.domain.repository.MessengerRepository
import javax.inject.Inject

class DeleteEventQueueUseCase @Inject constructor(private val repository: MessengerRepository) {

    suspend operator fun invoke(queueId: String) {
        return repository.deleteEventQueue(queueId)
    }
}