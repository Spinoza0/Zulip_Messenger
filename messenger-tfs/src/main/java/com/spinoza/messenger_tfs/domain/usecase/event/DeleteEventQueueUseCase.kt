package com.spinoza.messenger_tfs.domain.usecase.event

import com.spinoza.messenger_tfs.domain.repository.WebRepository
import javax.inject.Inject

class DeleteEventQueueUseCase @Inject constructor(private val repository: WebRepository) {

    suspend operator fun invoke(queueId: String) {
        return repository.deleteEventQueue(queueId)
    }
}