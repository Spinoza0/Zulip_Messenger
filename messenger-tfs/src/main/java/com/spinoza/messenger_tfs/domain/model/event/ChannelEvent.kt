package com.spinoza.messenger_tfs.domain.model.event

import com.spinoza.messenger_tfs.domain.model.Channel

data class ChannelEvent(
    val id: Long,
    val operation: EventOperation,
    val channel: Channel,
)