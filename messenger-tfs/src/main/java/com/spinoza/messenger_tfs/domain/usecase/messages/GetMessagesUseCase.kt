package com.spinoza.messenger_tfs.domain.usecase.messages

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesPageType
import com.spinoza.messenger_tfs.domain.model.MessagesResult
import com.spinoza.messenger_tfs.domain.repository.MessageRepository
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(private val repository: MessageRepository) {

    suspend operator fun invoke(
        messagesPageType: MessagesPageType,
        messagesFilter: MessagesFilter,
    ): Result<MessagesResult> {
        return repository.getMessages(messagesPageType, messagesFilter)
    }
}