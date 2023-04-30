package com.spinoza.messenger_tfs.domain.usecase.people

import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.MessengerRepository
import javax.inject.Inject

class GetAllUsersUseCase @Inject constructor(private val repository: MessengerRepository) {

    suspend operator fun invoke(): Result<List<User>> {
        return repository.getAllUsers()
    }
}