package com.spinoza.messenger_tfs.presentation.model.channels

sealed class ChannelsEvent {

    sealed class Ui : ChannelsEvent() {

        class Filter(val value: SearchQuery) : Ui()
    }
}