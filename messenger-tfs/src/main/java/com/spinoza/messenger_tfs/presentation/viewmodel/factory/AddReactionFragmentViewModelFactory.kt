package com.spinoza.messenger_tfs.presentation.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spinoza.messenger_tfs.domain.usecase.UpdateReactionUseCase
import com.spinoza.messenger_tfs.presentation.viewmodel.AddReactionViewModel

class AddReactionFragmentViewModelFactory(
    private val updateReactionUseCase: UpdateReactionUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddReactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddReactionViewModel(updateReactionUseCase) as T
        }
        throw RuntimeException("Unknown view model type")
    }
}