package com.spinoza.messenger_tfs.domain.usecase.messages

import com.spinoza.messenger_tfs.domain.repository.MessengerRepository
import javax.inject.Inject

class GetOwnUserIdUseCase @Inject constructor(private val repository: MessengerRepository) {

    suspend operator fun invoke(): Result<Long> {
        return repository.getOwnUserId()
    }
}