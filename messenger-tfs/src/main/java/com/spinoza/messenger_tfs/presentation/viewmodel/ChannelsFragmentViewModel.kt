package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.spinoza.messenger_tfs.domain.usecase.GetAllChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetSubscribedChannelsUseCase
import com.spinoza.messenger_tfs.presentation.model.ChannelsFragmentState
import com.spinoza.messenger_tfs.presentation.model.ChannelsFragmentState.SourceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChannelsFragmentViewModel(
    private val getAllChannelsUseCase: GetAllChannelsUseCase,
    private val getSubscribedChannelsUseCase: GetSubscribedChannelsUseCase,
) : ViewModel() {

    val state: StateFlow<ChannelsFragmentState>
        get() = _state.asStateFlow()

    private var source = SourceType.SUBSCRIBED

    private val _state =
        MutableStateFlow<ChannelsFragmentState>(
            ChannelsFragmentState.Source(SourceType.SUBSCRIBED)
        )

    init {
        getChannels()
    }

    private fun getChannels() {
        when (source) {
            SourceType.SUBSCRIBED -> {
                _state.value = ChannelsFragmentState.Channels(getAllChannelsUseCase())
            }
            SourceType.ALL -> {
                _state.value = ChannelsFragmentState.Channels(getSubscribedChannelsUseCase())
            }
        }
    }

    fun switchSource(newSource: SourceType) {
        val sourceChanged = newSource != source
        if (sourceChanged) {
            source = newSource
            getChannels()
            _state.value = ChannelsFragmentState.Source(source)
        }
    }
}