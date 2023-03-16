package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class UpdateReactionUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(messageId: Long, reactionValue: String) {
        repository.updateReaction(messageId, reactionValue)
    }
}