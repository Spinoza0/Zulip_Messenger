package com.spinoza.messenger_tfs.domain.usecase.login

import com.spinoza.messenger_tfs.domain.repository.WebRepository
import javax.inject.Inject

class LogInUseCase @Inject constructor(private val repository: WebRepository) {

    suspend operator fun invoke(email: String, password: String): Result<Boolean> {
        return repository.logIn(email, password)
    }
}