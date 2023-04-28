package com.spinoza.messenger_tfs.domain.usecase.messages

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import javax.inject.Inject

class GetOwnUserIdUseCase @Inject constructor(private val repository: MessagesRepository) {

    suspend operator fun invoke(): Result<Long> {
        return repository.getOwnUserId()
    }
}