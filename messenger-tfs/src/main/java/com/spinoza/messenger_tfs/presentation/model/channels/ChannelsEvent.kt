package com.spinoza.messenger_tfs.presentation.model.channels

import com.spinoza.messenger_tfs.presentation.model.SearchQuery

sealed class ChannelsEvent {

    sealed class Ui : ChannelsEvent() {

        class Filter(val value: SearchQuery) : Ui()
    }
}