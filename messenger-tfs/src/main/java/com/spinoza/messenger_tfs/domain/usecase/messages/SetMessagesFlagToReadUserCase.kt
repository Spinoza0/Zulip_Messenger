package com.spinoza.messenger_tfs.domain.usecase.messages

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import javax.inject.Inject

class SetMessagesFlagToReadUserCase @Inject constructor(private val repository: MessagesRepository) {

    suspend operator fun invoke(messageIds: List<Long>) {
        return repository.setMessagesFlagToRead(messageIds)
    }
}