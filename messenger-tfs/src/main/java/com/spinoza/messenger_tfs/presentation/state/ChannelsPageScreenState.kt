package com.spinoza.messenger_tfs.presentation.state

import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem

sealed class ChannelsPageScreenState {

    object Idle : ChannelsPageScreenState()

    object Loading : ChannelsPageScreenState()

    class Items(val value: List<DelegateAdapterItem>) : ChannelsPageScreenState()

    class TopicMessagesCountUpdate(val value: List<DelegateAdapterItem>) : ChannelsPageScreenState()
}