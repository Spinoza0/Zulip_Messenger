package com.spinoza.messenger_tfs.domain.usecase.profile

import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import javax.inject.Inject

class GetUserUseCase @Inject constructor(private val repository: MessagesRepository) {

    suspend operator fun invoke(userId: Long): Result<User> {
        return repository.getUser(userId)
    }
}