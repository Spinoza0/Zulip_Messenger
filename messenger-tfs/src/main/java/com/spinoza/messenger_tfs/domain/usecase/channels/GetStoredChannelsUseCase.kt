package com.spinoza.messenger_tfs.domain.usecase.channels

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.repository.DaoRepository
import javax.inject.Inject

class GetStoredChannelsUseCase @Inject constructor(private val repository: DaoRepository) {

    suspend operator fun invoke(channelsFilter: ChannelsFilter): Result<List<Channel>> {
        return repository.getStoredChannels(channelsFilter)
    }
}