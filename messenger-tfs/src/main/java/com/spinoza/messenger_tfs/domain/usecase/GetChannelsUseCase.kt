package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.RepositoryState

interface GetChannelsUseCase {

    operator fun invoke(): RepositoryState
}