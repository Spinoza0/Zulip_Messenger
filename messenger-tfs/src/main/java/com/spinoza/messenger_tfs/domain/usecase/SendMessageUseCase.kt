package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.MessagesResult
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

class SendMessageUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(
        message: Message,
        messagesFilter: MessagesFilter,
    ): Pair<RepositoryResult, MessagesResult?> {
        return repository.sendMessage(message, messagesFilter)
    }
}