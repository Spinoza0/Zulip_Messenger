package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class CheckLoginUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(email: String, password: String): Result<Boolean> {
        return repository.checkLogin(email, password)
    }
}