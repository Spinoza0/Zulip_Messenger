package com.spinoza.messenger_tfs.presentation.fragment.menu

import com.spinoza.messenger_tfs.databinding.ChannelItemBinding
import com.spinoza.messenger_tfs.presentation.model.ChannelItem
import com.spinoza.messenger_tfs.presentation.model.ChannelsFragmentState
import kotlinx.coroutines.flow.StateFlow

interface ChannelsPageParent {

    fun loadChannels(isAllChannels: Boolean)

    fun onChannelClickListener(
        isAllChannels: Boolean,
        channelItem: ChannelItem,
        itemBinding: ChannelItemBinding,
    )

    fun getState(isAllChannels: Boolean): StateFlow<ChannelsFragmentState>
}