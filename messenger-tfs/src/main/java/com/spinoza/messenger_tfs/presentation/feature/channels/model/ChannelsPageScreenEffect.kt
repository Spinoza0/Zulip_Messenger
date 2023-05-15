package com.spinoza.messenger_tfs.presentation.feature.channels.model

sealed class ChannelsPageScreenEffect {

    class ShowChannelMenu(
        val channelItem: ChannelItem,
        val isItemSubscribeVisible: Boolean,
        val isItemUnsubscribeVisible: Boolean,
        val isItemDeleteVisible: Boolean,
    ) : ChannelsPageScreenEffect()

    sealed class Failure : ChannelsPageScreenEffect() {

        class Network(val value: String) : Failure()

        class Error(val value: String) : Failure()
    }
}