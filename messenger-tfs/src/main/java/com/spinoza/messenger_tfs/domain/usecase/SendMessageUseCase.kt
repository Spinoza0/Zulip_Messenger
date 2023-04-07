package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

class SendMessageUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(
        content: String,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<Long> {
        return repository.sendMessage(content, messagesFilter)
    }
}