package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryState

class GetMessagesUseCase(private val repository: MessagesRepository) {

    operator fun invoke(): RepositoryState {
        return repository.getMessages()
    }
}