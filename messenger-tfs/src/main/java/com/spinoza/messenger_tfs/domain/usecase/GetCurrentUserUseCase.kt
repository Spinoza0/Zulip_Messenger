package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

class GetCurrentUserUseCase(private val repository: MessagesRepository) {

    operator fun invoke(): RepositoryResult<User> {
        return repository.getCurrentUser()
    }
}