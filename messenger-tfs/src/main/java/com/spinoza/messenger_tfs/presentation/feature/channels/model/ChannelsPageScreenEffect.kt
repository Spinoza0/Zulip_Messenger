package com.spinoza.messenger_tfs.presentation.feature.channels.model

import android.view.View

sealed class ChannelsPageScreenEffect {

    class ShowChannelMenu(
        val view: View,
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