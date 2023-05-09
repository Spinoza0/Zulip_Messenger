package com.spinoza.messenger_tfs.domain.usecase.messages

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesResult
import com.spinoza.messenger_tfs.domain.repository.DaoRepository
import javax.inject.Inject

class GetStoredMessagesUseCase @Inject constructor(private val repository: DaoRepository) {

    suspend operator fun invoke(messagesFilter: MessagesFilter): Result<MessagesResult> {
        return repository.getStoredMessages(messagesFilter)
    }
}