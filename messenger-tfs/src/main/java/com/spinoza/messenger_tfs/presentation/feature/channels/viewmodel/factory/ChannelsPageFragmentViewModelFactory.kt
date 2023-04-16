package com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.di.ChannelIsSubscribed
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.ChannelsPageFragmentViewModel
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
class ChannelsPageFragmentViewModelFactory @Inject constructor(
    @ChannelIsSubscribed private val isSubscribed: Boolean,
    private val router: Router,
    private val getTopicsUseCase: GetTopicsUseCase,
    private val getChannelsUseCase: GetChannelsUseCase,
    private val getTopicUseCase: GetTopicUseCase,
    private val getChannelEventsUseCase: GetChannelEventsUseCase,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChannelsPageFragmentViewModel(
            isSubscribed,
            router,
            getTopicsUseCase,
            getChannelsUseCase,
            getTopicUseCase,
            getChannelEventsUseCase,
        ) as T
    }
}