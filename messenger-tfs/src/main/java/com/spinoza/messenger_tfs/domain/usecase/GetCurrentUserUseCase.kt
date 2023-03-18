package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class GetCurrentUserUseCase(private val repository: MessagesRepository) {

    operator fun invoke(): User {
        return repository.getCurrentUser()
    }
}