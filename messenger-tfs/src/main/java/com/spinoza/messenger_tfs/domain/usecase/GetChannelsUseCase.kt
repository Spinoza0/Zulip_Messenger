package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

interface GetChannelsUseCase {

    suspend operator fun invoke(): Pair<RepositoryResult, List<Channel>>
}