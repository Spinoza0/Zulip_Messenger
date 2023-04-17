package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class GetApiKeyUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(
        storedApiKey: String,
        email: String,
        password: String,
    ): Result<String> {
        return repository.getApiKey(storedApiKey, email, password)
    }
}