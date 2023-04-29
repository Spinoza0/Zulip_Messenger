package com.spinoza.messenger_tfs.domain.usecase.messages

import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesResult
import com.spinoza.messenger_tfs.domain.repository.MessengerRepository
import javax.inject.Inject

class UpdateReactionUseCase @Inject constructor(private val repository: MessengerRepository) {

    suspend operator fun invoke(
        messageId: Long,
        emoji: Emoji,
        messagesFilter: MessagesFilter,
    ): Result<MessagesResult> {
        return repository.updateReaction(messageId, emoji, messagesFilter)
    }
}