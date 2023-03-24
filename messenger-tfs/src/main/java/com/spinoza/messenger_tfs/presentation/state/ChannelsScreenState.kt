package com.spinoza.messenger_tfs.presentation.state

sealed class ChannelsScreenState {

    object Idle: ChannelsScreenState()

    class Search(val value: String) : ChannelsScreenState()
}