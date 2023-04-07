package com.spinoza.messenger_tfs.presentation.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.viewmodel.ChannelsPageFragmentViewModel

@Suppress("UNCHECKED_CAST")
class ChannelsPageFragmentViewModelFactory(
    private val isAllChannels: Boolean,
    private val getTopicsUseCase: GetTopicsUseCase,
    private val getChannelsUseCase: GetChannelsUseCase,
    private val getTopicUseCase: GetTopicUseCase,
    private val registerEventQueueUseCase: RegisterEventQueueUseCase,
    private val deleteEventQueueUseCase: DeleteEventQueueUseCase,
    private val getChannelEventsUseCase: GetChannelEventsUseCase,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChannelsPageFragmentViewModel(
            isAllChannels,
            getTopicsUseCase,
            getChannelsUseCase,
            getTopicUseCase,
            registerEventQueueUseCase,
            deleteEventQueueUseCase,
            getChannelEventsUseCase
        ) as T
    }
}