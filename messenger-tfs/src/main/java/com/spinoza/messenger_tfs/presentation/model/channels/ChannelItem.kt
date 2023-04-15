package com.spinoza.messenger_tfs.presentation.model.channels

import com.spinoza.messenger_tfs.domain.model.Channel

data class ChannelItem(
    val channel: Channel,
    val isAllChannelsItem: Boolean,
    val isFolded: Boolean,
)