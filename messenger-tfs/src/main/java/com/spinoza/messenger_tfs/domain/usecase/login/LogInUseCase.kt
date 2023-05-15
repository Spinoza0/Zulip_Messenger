package com.spinoza.messenger_tfs.domain.usecase.login

import com.spinoza.messenger_tfs.domain.repository.UserRepository
import javax.inject.Inject

class LogInUseCase @Inject constructor(private val repository: UserRepository) {

    suspend operator fun invoke(email: String, password: String): Result<Boolean> {
        return repository.logIn(email, password)
    }
}