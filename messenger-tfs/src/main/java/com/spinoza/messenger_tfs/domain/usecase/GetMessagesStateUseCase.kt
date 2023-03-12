package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.MessagesState
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import kotlinx.coroutines.flow.MutableSharedFlow

class GetMessagesStateUseCase(private val repository: MessagesRepository) {
    operator fun invoke(): MutableSharedFlow<MessagesState> {
        return repository.getMessagesState()
    }
}