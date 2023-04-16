package com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.ChannelsPageFragmentViewModel

@Suppress("UNCHECKED_CAST")
class ChannelsPageFragmentViewModelFactory(
    private val isAllChannels: Boolean,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChannelsPageFragmentViewModel(isAllChannels) as T
    }
}