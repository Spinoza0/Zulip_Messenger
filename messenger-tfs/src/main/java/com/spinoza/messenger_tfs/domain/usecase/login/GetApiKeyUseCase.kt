package com.spinoza.messenger_tfs.domain.usecase.login

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import javax.inject.Inject

class GetApiKeyUseCase @Inject constructor(private val repository: MessagesRepository) {

    suspend operator fun invoke(
        storedApiKey: String,
        email: String,
        password: String,
    ): Result<String> {
        return repository.getApiKey(storedApiKey, email, password)
    }
}