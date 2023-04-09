package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class GetUsersByFilterUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(usersFilter: String): Result<List<User>> {
        return repository.getUsersByFilter(usersFilter)
    }
}