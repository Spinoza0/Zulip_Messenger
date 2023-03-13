package com.spinoza.messenger_tfs.presentation.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spinoza.messenger_tfs.domain.usecase.UpdateReactionUseCase
import com.spinoza.messenger_tfs.presentation.viewmodel.ChooseReactionViewModel

class ChooseReactionViewModelFactory(
    private val updateReactionUseCase: UpdateReactionUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChooseReactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChooseReactionViewModel(updateReactionUseCase) as T
        }
        throw RuntimeException("Unknown view model type")
    }
}