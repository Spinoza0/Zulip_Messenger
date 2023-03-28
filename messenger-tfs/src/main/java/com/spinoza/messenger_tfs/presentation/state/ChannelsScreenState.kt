package com.spinoza.messenger_tfs.presentation.state

import com.spinoza.messenger_tfs.presentation.model.SearchQuery

sealed class ChannelsScreenState {

    object Idle : ChannelsScreenState()

    class Filter(val value: SearchQuery) : ChannelsScreenState()
}