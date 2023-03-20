package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

class GetAllUsersUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(): Pair<RepositoryResult, List<User>> {
        return repository.getAllUsers()
    }
}