package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.model.ChannelFilter
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryState

class UpdateReactionUseCase(private val repository: MessagesRepository) {

    suspend operator fun invoke(
        messageId: Long,
        reactionValue: String,
        channelFilter: ChannelFilter,
    ): RepositoryState {
        return repository.updateReaction(messageId, reactionValue, channelFilter)
    }
}