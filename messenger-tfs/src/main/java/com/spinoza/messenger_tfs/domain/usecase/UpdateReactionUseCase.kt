package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.MessagesResult
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

class UpdateReactionUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(
        messageId: Long,
        reactionValue: String,
        messagesFilter: MessagesFilter,
    ): Pair<RepositoryResult, MessagesResult?> {
        return repository.updateReaction(messageId, reactionValue, messagesFilter)
    }
}