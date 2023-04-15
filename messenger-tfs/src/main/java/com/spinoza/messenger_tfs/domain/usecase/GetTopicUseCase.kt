package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class GetTopicUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(messagesFilter: MessagesFilter): Result<Topic> {
        return repository.getTopic(messagesFilter)
    }
}