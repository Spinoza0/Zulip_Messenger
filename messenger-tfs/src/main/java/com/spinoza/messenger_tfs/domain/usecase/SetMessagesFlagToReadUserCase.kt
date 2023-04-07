package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class SetMessagesFlagToReadUserCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(messageIds: List<Long>) {
        return repository.setMessagesFlagToRead(messageIds)
    }
}