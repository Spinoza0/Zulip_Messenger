package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

class GetUserUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(userId: Long): Pair<RepositoryResult, User?> {
        return repository.getUser(userId)
    }
}