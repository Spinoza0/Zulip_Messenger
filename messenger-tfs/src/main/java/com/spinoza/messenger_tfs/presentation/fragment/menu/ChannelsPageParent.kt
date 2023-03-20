package com.spinoza.messenger_tfs.presentation.fragment.menu

import com.spinoza.messenger_tfs.presentation.state.ChannelsScreenState
import kotlinx.coroutines.flow.StateFlow

interface ChannelsPageParent {

    fun loadItems(isAllChannels: Boolean)

    fun getState(isAllChannels: Boolean): StateFlow<ChannelsScreenState>
}