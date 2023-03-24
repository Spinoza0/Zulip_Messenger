package com.spinoza.messenger_tfs.presentation.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.viewmodel.ChannelsFragmentViewModel

@Suppress("UNCHECKED_CAST")
class ChannelsFragmentViewModelFactory(
    private val isAllChannels: Boolean,
    private val getTopicsUseCase: GetTopicsUseCase,
    private val getSubscribedChannelsUseCase: GetSubscribedChannelsUseCase,
    private val getAllChannelsUseCase: GetAllChannelsUseCase,
    private val getTopicUseCase: GetTopicUseCase,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChannelsFragmentViewModel(
            isAllChannels,
            getTopicsUseCase,
            getSubscribedChannelsUseCase,
            getAllChannelsUseCase,
            getTopicUseCase
        ) as T
    }
}