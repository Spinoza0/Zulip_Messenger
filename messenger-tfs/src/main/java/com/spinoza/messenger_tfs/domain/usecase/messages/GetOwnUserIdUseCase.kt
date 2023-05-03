package com.spinoza.messenger_tfs.domain.usecase.messages

import com.spinoza.messenger_tfs.domain.repository.WebRepository
import javax.inject.Inject

class GetOwnUserIdUseCase @Inject constructor(private val repository: WebRepository) {

    suspend operator fun invoke(): Result<Long> {
        return repository.getOwnUserId()
    }
}