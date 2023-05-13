package com.spinoza.messenger_tfs.domain.usecase.messages

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.repository.MessageRepository
import javax.inject.Inject

class GetUpdatedMessageFilterUserCase @Inject constructor(
    private val repository: MessageRepository,
) {

    suspend operator fun invoke(messagesFilter: MessagesFilter): MessagesFilter {
        return repository.getUpdatedMessageFilter(messagesFilter)
    }
}