package com.spinoza.messenger_tfs.domain.usecase.channels

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.repository.WebRepository
import javax.inject.Inject

class GetStoredChannelsUseCase @Inject constructor(private val repository: WebRepository) {

    suspend operator fun invoke(channelsFilter: ChannelsFilter): Result<List<Channel>> {
        return repository.getStoredChannels(channelsFilter)
    }
}