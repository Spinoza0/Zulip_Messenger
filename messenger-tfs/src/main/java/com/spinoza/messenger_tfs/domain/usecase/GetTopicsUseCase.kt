package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import javax.inject.Inject

class GetTopicsUseCase @Inject constructor(private val repository: MessagesRepository) {

    suspend operator fun invoke(channel: Channel): Result<List<Topic>> {
        return repository.getTopics(channel)
    }
}