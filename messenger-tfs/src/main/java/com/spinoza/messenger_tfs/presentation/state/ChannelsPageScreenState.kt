package com.spinoza.messenger_tfs.presentation.state

import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem

sealed class ChannelsPageScreenState {

    object Loading : ChannelsPageScreenState()

    class Items(val value: List<DelegateAdapterItem>) : ChannelsPageScreenState()

    class TopicMessagesCountUpdate(val value: List<DelegateAdapterItem>) : ChannelsPageScreenState()

    class Filter(val value: ChannelsFilter) : ChannelsPageScreenState()
}