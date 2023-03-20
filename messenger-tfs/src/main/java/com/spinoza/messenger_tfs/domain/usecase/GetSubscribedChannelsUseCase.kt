package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

class GetSubscribedChannelsUseCase(private val repository: MessagesRepository) :
    GetChannelsUseCase {

    override suspend operator fun invoke(): Pair<RepositoryResult, List<Channel>> {
        return repository.getSubscribedChannels()
    }
}