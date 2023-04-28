package com.spinoza.messenger_tfs.domain.usecase.people

import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import javax.inject.Inject

class GetUsersByFilterUseCase @Inject constructor(private val repository: MessagesRepository) {

    suspend operator fun invoke(usersFilter: String): Result<List<User>> {
        return repository.getUsersByFilter(usersFilter)
    }
}