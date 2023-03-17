package com.spinoza.messenger_tfs.presentation.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.viewmodel.ChannelsFragmentViewModel

@Suppress("UNCHECKED_CAST")
class ChannelsFragmentViewModelFactory(
    private val getChannelsUseCase: GetChannelsUseCase,
    private val getTopicsUseCase: GetTopicsUseCase,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChannelsFragmentViewModel(getChannelsUseCase, getTopicsUseCase) as T
    }
}