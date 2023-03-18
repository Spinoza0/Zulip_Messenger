package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryState

class GetAllUsersUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(): RepositoryState {
        return repository.getAllUsers()
    }
}