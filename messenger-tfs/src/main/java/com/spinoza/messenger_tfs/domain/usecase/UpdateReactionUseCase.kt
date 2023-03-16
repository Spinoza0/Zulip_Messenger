package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class UpdateReactionUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(messageId: Long, userId: Long, reactionValue: String) {
        repository.updateReaction(messageId, userId, reactionValue)
    }
}