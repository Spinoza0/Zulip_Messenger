package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class GetOwnUserIdUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(): Result<Long> {
        return repository.getOwnUserId()
    }
}