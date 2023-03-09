package com.spinoza.messenger_tfs.domain.usecase

import androidx.lifecycle.LiveData
import com.spinoza.messenger_tfs.domain.model.RepositoryState
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class GetStateUseCase(private val repository: MessagesRepository) {
    operator fun invoke(): LiveData<RepositoryState> {
        return repository.getState()
    }
}