package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

class GetOwnUserIdUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(): RepositoryResult<Long> {
        return repository.getOwnUserId()
    }
}