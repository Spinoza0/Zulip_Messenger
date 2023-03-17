package com.spinoza.messenger_tfs.presentation.model

import com.spinoza.messenger_tfs.databinding.ChannelItemBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.Topic

sealed class ChannelsFragmentState {

    object Idle : ChannelsFragmentState()

    class Error(val text: String) : ChannelsFragmentState()

    class Channels(val channels: List<Channel>) : ChannelsFragmentState()

    class Topics(
        val topics: List<Topic>,
        val channel: Channel,
        val binding: ChannelItemBinding,
    ) : ChannelsFragmentState()
}