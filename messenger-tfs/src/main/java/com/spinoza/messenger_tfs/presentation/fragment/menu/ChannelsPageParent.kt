package com.spinoza.messenger_tfs.presentation.fragment.menu

import com.spinoza.messenger_tfs.databinding.ChannelItemBinding
import com.spinoza.messenger_tfs.presentation.model.ChannelItem
import com.spinoza.messenger_tfs.presentation.model.ChannelsFragmentState
import kotlinx.coroutines.flow.StateFlow

interface ChannelsPageParent {

    fun getChannels(allChannels: Boolean)

    fun onChannelClickListener(
        allChannels: Boolean,
        channelItem: ChannelItem,
        itemBinding: ChannelItemBinding,
    )

    fun getState(allChannels: Boolean): StateFlow<ChannelsFragmentState>
}