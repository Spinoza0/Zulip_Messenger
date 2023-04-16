package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import javax.inject.Inject

class CheckLoginUseCase @Inject constructor(private val repository: MessagesRepository) {

    suspend operator fun invoke(apiKey: String, email: String, password: String): Result<String> {
        return repository.checkLogin(apiKey, email, password)
    }
}