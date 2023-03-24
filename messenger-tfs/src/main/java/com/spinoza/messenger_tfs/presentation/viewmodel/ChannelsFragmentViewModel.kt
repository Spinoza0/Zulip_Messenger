package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.spinoza.messenger_tfs.presentation.state.ChannelsScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChannelsFragmentViewModel() : ViewModel() {

    val state: StateFlow<ChannelsScreenState>
        get() = _state.asStateFlow()
    private val _state =
        MutableStateFlow<ChannelsScreenState>(ChannelsScreenState.Idle)

    fun doOnTextChanged(text: CharSequence?) {
        // TODO
        _state.value = ChannelsScreenState.Search(text.toString())
    }
}