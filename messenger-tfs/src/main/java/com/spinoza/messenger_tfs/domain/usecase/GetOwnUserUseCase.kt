package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class GetOwnUserUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(): Result<User> {
        return repository.getOwnUser()
    }
}