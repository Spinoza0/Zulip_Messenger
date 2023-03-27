package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

class GetChannelsUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(channelsFilter: ChannelsFilter): RepositoryResult<List<Channel>> {
        return repository.getChannels(channelsFilter)
    }
}