package com.spinoza.messenger_tfs.domain.usecase.channels

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.repository.ChannelRepository
import javax.inject.Inject

class GetTopicsUseCase @Inject constructor(private val repository: ChannelRepository) {

    suspend operator fun invoke(channel: Channel): Result<List<Topic>> {
        return repository.getTopics(channel)
    }
}