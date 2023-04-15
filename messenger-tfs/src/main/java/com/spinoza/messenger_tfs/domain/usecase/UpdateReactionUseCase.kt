package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesResult
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class UpdateReactionUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(
        messageId: Long,
        emoji: Emoji,
        messagesFilter: MessagesFilter,
    ): Result<MessagesResult> {
        return repository.updateReaction(messageId, emoji, messagesFilter)
    }
}