package com.spinoza.messenger_tfs.domain.usecase.messages

import com.spinoza.messenger_tfs.domain.repository.WebRepository
import com.spinoza.messenger_tfs.domain.util.EMPTY_STRING
import javax.inject.Inject

class EditMessageUseCase @Inject constructor(private val repository: WebRepository) {

    suspend operator fun invoke(
        messageId: Long,
        topic: String = EMPTY_STRING,
        content: String = EMPTY_STRING,
    ): Result<Boolean> {
        return repository.editMessage(messageId, topic, content)
    }
}