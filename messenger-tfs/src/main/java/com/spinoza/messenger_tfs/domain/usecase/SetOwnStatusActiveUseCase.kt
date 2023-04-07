package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class SetOwnStatusActiveUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke() {
        repository.setOwnStatusActive()
    }
}