package com.spinoza.messenger_tfs.domain.usecase.messages

import com.spinoza.messenger_tfs.domain.repository.MessageRepository
import javax.inject.Inject

class GetMessageRawContentUseCase @Inject constructor(private val repository: MessageRepository) {

    suspend operator fun invoke(messageId: Long, default: String): String {
        return repository.getMessageRawContent(messageId, default)
    }
}