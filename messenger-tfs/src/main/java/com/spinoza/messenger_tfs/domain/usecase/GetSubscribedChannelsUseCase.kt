package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class GetSubscribedChannelsUseCase(private val repository: MessagesRepository) {

    operator fun invoke() {
        repository.getSubscribedChannels()
    }
}