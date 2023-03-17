package com.spinoza.messenger_tfs.presentation.model

import com.spinoza.messenger_tfs.databinding.ChannelItemBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.Topic

sealed class ChannelsFragmentState {

    class Error(val text: String) : ChannelsFragmentState()

    class Source(val type: SourceType) : ChannelsFragmentState()

    class Channels(val channels: List<Channel>) : ChannelsFragmentState()

    class Topics(
        val topics: List<Topic>,
        val channel: Channel,
        val binding: ChannelItemBinding,
    ) : ChannelsFragmentState()

    enum class SourceType {
        SUBSCRIBED, ALL
    }
}