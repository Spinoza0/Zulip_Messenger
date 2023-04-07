package com.spinoza.messenger_tfs.presentation.model.channelspage

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter

sealed class ChannelsPageEffect {

    sealed class Failure : ChannelsPageEffect() {

        class Network(val value: String) : Failure()

        class LoadingChannels(val channelsFilter: ChannelsFilter, val value: String) : Failure()

        class LoadingChannelTopics(val channel: Channel, val value: String) : Failure()
    }
}