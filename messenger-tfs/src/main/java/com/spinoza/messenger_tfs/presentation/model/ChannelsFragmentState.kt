package com.spinoza.messenger_tfs.presentation.model

import com.spinoza.messenger_tfs.databinding.ChannelItemBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

sealed class ChannelsFragmentState {

    object Loading : ChannelsFragmentState()

    class Error(val value: RepositoryResult) : ChannelsFragmentState()

    class Channels(val channels: List<Channel>) : ChannelsFragmentState()

    class Topics(
        val topics: List<Topic>,
        val channel: Channel,
        val binding: ChannelItemBinding,
    ) : ChannelsFragmentState()
}