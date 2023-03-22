package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.MessagesResult
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

class GetMessagesUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(
        messagesFilter: MessagesFilter,
    ): Pair<RepositoryResult, MessagesResult?> {
        return repository.getMessages(messagesFilter)
    }
}