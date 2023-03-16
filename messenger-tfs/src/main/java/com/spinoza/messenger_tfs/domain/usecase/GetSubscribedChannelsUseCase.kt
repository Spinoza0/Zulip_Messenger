package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryState

class GetSubscribedChannelsUseCase(private val repository: MessagesRepository) {

    operator fun invoke(): RepositoryState {
        return repository.getSubscribedChannels()
    }
}