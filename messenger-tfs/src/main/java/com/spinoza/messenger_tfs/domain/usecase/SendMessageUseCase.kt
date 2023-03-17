package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.ChannelFilter
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryState

class SendMessageUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(message: Message, channelFilter: ChannelFilter): RepositoryState {
        return repository.sendMessage(message, channelFilter)
    }
}