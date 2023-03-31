package com.spinoza.messenger_tfs.presentation.state

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem

sealed class ChannelsPageScreenState {

    object Loading : ChannelsPageScreenState()

    class Items(val value: List<DelegateAdapterItem>) : ChannelsPageScreenState()

    class TopicMessagesCountUpdate(val value: List<DelegateAdapterItem>) : ChannelsPageScreenState()

    sealed class Failure : ChannelsPageScreenState() {

        class LoadingChannels(val channelsFilter: ChannelsFilter, val value: String) : Failure()

        class LoadingChannelTopics(val channel: Channel, val value: String) : Failure()
    }
}