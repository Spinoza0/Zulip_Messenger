package com.spinoza.messenger_tfs.domain.usecase.channels

import com.spinoza.messenger_tfs.domain.repository.ChannelRepository
import javax.inject.Inject

class GetChannelSubscriptionStatusUseCase @Inject constructor(
    private val repository: ChannelRepository,
) {

    suspend operator fun invoke(channelId: Long): Result<Boolean> {
        return repository.getChannelSubscriptionStatus(channelId)
    }
}