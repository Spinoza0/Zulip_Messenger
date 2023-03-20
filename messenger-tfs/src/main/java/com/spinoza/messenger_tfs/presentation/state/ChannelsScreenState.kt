package com.spinoza.messenger_tfs.presentation.state

import com.spinoza.messenger_tfs.databinding.ChannelItemBinding
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.presentation.model.ChannelItem

sealed class ChannelsScreenState {

    object Loading : ChannelsScreenState()

    class Error(val value: RepositoryResult) : ChannelsScreenState()

    class Channels(val channels: List<ChannelItem>) : ChannelsScreenState()

    class Topics(
        val topics: List<Topic>,
        val channel: ChannelItem,
        val binding: ChannelItemBinding,
    ) : ChannelsScreenState()
}