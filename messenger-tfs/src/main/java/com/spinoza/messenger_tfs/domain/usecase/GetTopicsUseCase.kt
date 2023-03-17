package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryState

class GetTopicsUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(channelId: Long): RepositoryState {
        return repository.getTopics(channelId)
    }
}