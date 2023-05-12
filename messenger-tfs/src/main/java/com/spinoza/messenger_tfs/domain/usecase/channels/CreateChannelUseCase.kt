package com.spinoza.messenger_tfs.domain.usecase.channels

import com.spinoza.messenger_tfs.domain.repository.WebRepository
import javax.inject.Inject

class CreateChannelUseCase @Inject constructor(private val repository: WebRepository) {

    suspend operator fun invoke(name: String, description: String): Result<Boolean> {
        return repository.createChannel(name, description)
    }
}