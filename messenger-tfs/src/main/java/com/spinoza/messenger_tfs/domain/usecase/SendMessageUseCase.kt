package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(private val repository: MessagesRepository) {

    suspend operator fun invoke(
        content: String,
        messagesFilter: MessagesFilter,
    ): Result<Long> {
        return repository.sendMessage(content, messagesFilter)
    }
}