package com.spinoza.messenger_tfs.domain.usecase.messages

import com.spinoza.messenger_tfs.domain.repository.MessageRepository
import com.spinoza.messenger_tfs.domain.util.EMPTY_STRING
import javax.inject.Inject

class EditMessageUseCase @Inject constructor(private val repository: MessageRepository) {

    suspend operator fun invoke(
        messageId: Long,
        topic: String = EMPTY_STRING,
        content: String = EMPTY_STRING,
    ): Result<Long> {
        return repository.editMessage(messageId, topic, content)
    }
}