package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class GetUserIdUseCase(private val repository: MessagesRepository) {

    operator fun invoke(): Long {
        return repository.getUserId()
    }
}