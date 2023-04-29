package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import javax.inject.Inject

class GetStoredChannelsUseCase @Inject constructor(private val repository: MessagesRepository) {

    suspend operator fun invoke(channelsFilter: ChannelsFilter): Result<List<Channel>> {
        return repository.getStoredChannels(channelsFilter)
    }
}