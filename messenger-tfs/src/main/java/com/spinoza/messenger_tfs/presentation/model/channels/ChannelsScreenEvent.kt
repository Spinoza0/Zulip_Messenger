package com.spinoza.messenger_tfs.presentation.model.channels

sealed class ChannelsScreenEvent {

    sealed class Ui : ChannelsScreenEvent() {

        class Filter(val value: SearchQuery) : Ui()
    }
}