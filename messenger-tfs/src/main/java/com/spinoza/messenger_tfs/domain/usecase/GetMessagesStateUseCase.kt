package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.RepositoryState
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import kotlinx.coroutines.flow.StateFlow

class GetRepositoryStateUseCase(private val repository: MessagesRepository) {

    operator fun invoke(): StateFlow<RepositoryState> {
        return repository.getState()
    }
}