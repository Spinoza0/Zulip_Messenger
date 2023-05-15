package com.spinoza.messenger_tfs.domain.usecase.messages

import com.spinoza.messenger_tfs.domain.repository.MessageRepository
import javax.inject.Inject

class SetMessagesFlagToReadUserCase @Inject constructor(private val repository: MessageRepository) {

    suspend operator fun invoke(messageIds: List<Long>) {
        return repository.setMessagesFlagToRead(messageIds)
    }
}