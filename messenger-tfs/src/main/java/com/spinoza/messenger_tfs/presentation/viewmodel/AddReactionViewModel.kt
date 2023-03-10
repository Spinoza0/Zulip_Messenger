package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.domain.usecase.UpdateReactionUseCase
import kotlinx.coroutines.launch

class AddReactionViewModel(
    private val updateReactionUseCase: UpdateReactionUseCase,
) : ViewModel() {

    fun updateReaction(messageId: Int, reactionValue: String) {
        viewModelScope.launch {
            updateReactionUseCase(messageId, reactionValue)
        }
    }
}