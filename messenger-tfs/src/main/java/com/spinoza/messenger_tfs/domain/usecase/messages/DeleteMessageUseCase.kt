package com.spinoza.messenger_tfs.domain.usecase.messages

import com.spinoza.messenger_tfs.domain.repository.WebRepository
import javax.inject.Inject

class DeleteMessageUseCase @Inject constructor(private val repository: WebRepository) {

    suspend operator fun invoke(messageId: Long): Result<Boolean> {
        return repository.deleteMessage(messageId)
    }
}