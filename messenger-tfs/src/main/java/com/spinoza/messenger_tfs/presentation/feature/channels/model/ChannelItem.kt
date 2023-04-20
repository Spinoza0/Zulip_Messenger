package com.spinoza.messenger_tfs.presentation.feature.channels.model

import com.spinoza.messenger_tfs.domain.model.Channel

data class ChannelItem(
    val channel: Channel,
    val isFolded: Boolean,
)