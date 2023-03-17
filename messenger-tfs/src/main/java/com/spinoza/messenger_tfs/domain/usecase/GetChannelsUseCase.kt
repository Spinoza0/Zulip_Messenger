package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.RepositoryState

interface GetChannelsUseCase {

    suspend operator fun invoke(): RepositoryState
}