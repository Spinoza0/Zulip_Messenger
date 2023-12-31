package com.spinoza.messenger_tfs.domain.usecase.channels

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.repository.ChannelRepository
import javax.inject.Inject

class GetTopicUseCase @Inject constructor(private val repository: ChannelRepository) {

    suspend operator fun invoke(messagesFilter: MessagesFilter): Result<Topic> {
        return repository.getTopic(messagesFilter)
    }
}