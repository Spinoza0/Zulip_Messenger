package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.ChannelFilter
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryState

class GetMessagesUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(channelFilter: ChannelFilter): RepositoryState {
        return repository.getMessages(channelFilter)
    }
}