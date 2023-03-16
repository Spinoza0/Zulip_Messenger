package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.RepositoryState
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import kotlinx.coroutines.flow.SharedFlow

class GetRepositoryStateUseCase(private val repository: MessagesRepository) {

    operator fun invoke(userId: Int): SharedFlow<RepositoryState> {
        return repository.getState(userId)
    }
}