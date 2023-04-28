package com.spinoza.messenger_tfs.domain.usecase.profile

import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import javax.inject.Inject

class GetOwnUserUseCase @Inject constructor(private val repository: MessagesRepository) {

    suspend operator fun invoke(): Result<User> {
        return repository.getOwnUser()
    }
}