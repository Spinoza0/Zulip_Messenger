package com.spinoza.messenger_tfs.presentation.feature.channels.model

sealed class ChannelsScreenEvent {

    sealed class Ui : ChannelsScreenEvent() {

        class Filter(val value: SearchQuery) : Ui()
    }
}