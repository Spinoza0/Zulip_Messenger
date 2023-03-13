package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.domain.usecase.UpdateReactionUseCase
import kotlinx.coroutines.launch

class ChooseReactionViewModel(
    private val updateReactionUseCase: UpdateReactionUseCase,
) : ViewModel() {

    fun updateReaction(messageId: Int, userId: Int, reactionValue: String) {
        viewModelScope.launch {
            updateReactionUseCase(messageId, userId, reactionValue)
        }
    }
}