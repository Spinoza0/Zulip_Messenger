package com.spinoza.messenger_tfs.presentation.model

import com.spinoza.messenger_tfs.domain.model.Channel

sealed class ChannelsFragmentState {

    class Error(val text: String) : ChannelsFragmentState()

    class Source(val type: SourceType) : ChannelsFragmentState()

    class Channels(val channels: List<Channel>) : ChannelsFragmentState()

    enum class SourceType {
        SUBSCRIBED, ALL
    }
}