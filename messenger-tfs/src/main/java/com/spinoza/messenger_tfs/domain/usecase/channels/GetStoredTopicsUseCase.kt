package com.spinoza.messenger_tfs.domain.usecase.channels

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.repository.MessengerRepository
import javax.inject.Inject

class GetStoredTopicsUseCase @Inject constructor(private val repository: MessengerRepository) {

    suspend operator fun invoke(channel: Channel): Result<List<Topic>> {
        return repository.getStoredTopics(channel)
    }
}