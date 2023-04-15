package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class GetUserUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(userId: Long): Result<User> {
        return repository.getUser(userId)
    }
}