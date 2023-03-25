package com.spinoza.messenger_tfs.presentation.state

sealed class ChannelsScreenState {

    object Idle : ChannelsScreenState()

    class Filter(val screenPosition: Int, val value: String) : ChannelsScreenState()
}