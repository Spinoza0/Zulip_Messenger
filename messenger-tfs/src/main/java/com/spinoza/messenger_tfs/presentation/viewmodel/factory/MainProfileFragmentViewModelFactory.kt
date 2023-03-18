package com.spinoza.messenger_tfs.presentation.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.viewmodel.MainProfileFragmentViewModel

@Suppress("UNCHECKED_CAST")
class MainProfileFragmentViewModelFactory(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainProfileFragmentViewModel(getCurrentUserUseCase) as T
    }
}