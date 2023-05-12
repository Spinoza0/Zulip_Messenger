package com.spinoza.messenger_tfs.domain.usecase.channels

import com.spinoza.messenger_tfs.domain.repository.WebRepository
import javax.inject.Inject

class UnsubscribeFromChannelUseCase @Inject constructor(
    private val repository: WebRepository,
) {

    suspend operator fun invoke(name: String): Result<Boolean> {
        return repository.unsubscribeFromChannel(name)
    }
}