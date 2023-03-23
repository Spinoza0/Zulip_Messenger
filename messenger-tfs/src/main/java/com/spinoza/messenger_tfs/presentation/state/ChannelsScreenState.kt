package com.spinoza.messenger_tfs.presentation.state

import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem

sealed class ChannelsScreenState {

    object Loading : ChannelsScreenState()

    class Items(val value: List<DelegateAdapterItem>) : ChannelsScreenState()
}