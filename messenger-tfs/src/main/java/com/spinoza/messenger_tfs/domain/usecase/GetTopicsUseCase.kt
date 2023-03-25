package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

class GetTopicsUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(channel: Channel): RepositoryResult<List<Topic>> {
        return repository.getTopics(channel)
    }
}