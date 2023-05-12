package com.spinoza.messenger_tfs.domain.usecase.messages

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.repository.WebRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(private val repository: WebRepository) {

    suspend operator fun invoke(
        subject: String,
        content: String,
        messagesFilter: MessagesFilter,
    ): Result<Long> {
        return repository.sendMessage(subject, content, messagesFilter)
    }
}