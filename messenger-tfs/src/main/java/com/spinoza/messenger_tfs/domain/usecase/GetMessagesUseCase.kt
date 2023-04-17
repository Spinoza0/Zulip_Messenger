package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesResult
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(private val repository: MessagesRepository) {

    suspend operator fun invoke(messagesFilter: MessagesFilter): Result<MessagesResult> {
        return repository.getMessages(messagesFilter)
    }
}