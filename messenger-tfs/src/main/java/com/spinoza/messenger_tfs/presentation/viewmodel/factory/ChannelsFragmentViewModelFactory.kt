package com.spinoza.messenger_tfs.presentation.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.adapter.channels.TopicDelegateConfig
import com.spinoza.messenger_tfs.presentation.model.ChannelItem
import com.spinoza.messenger_tfs.presentation.viewmodel.ChannelsFragmentViewModel

@Suppress("UNCHECKED_CAST")
class ChannelsFragmentViewModelFactory(
    private val getTopicsUseCase: GetTopicsUseCase,
    private val getSubscribedChannelsUseCase: GetSubscribedChannelsUseCase,
    private val getAllChannelsUseCase: GetAllChannelsUseCase,
    private val onChannelClickListener: (ChannelItem) -> Unit,
    private val topicDelegateConfig: TopicDelegateConfig,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChannelsFragmentViewModel(
            getTopicsUseCase,
            getSubscribedChannelsUseCase,
            getAllChannelsUseCase,
            onChannelClickListener,
            topicDelegateConfig
        ) as T
    }
}