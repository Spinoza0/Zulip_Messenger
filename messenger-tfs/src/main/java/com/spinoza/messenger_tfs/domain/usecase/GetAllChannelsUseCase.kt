package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryState

class GetAllChannelsUseCase(private val repository: MessagesRepository) : GetChannelsUseCase {

    override suspend operator fun invoke(): RepositoryState {
        return repository.getAllChannels()
    }
}