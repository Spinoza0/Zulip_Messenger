package com.spinoza.messenger_tfs.presentation.model

import com.spinoza.messenger_tfs.domain.model.Channel

data class ChannelItem(
    val channel: Channel,
    val type: Type,
) {
    enum class Type { FOLDED, UNFOLDED }
}

fun Channel.toChannelItem(): ChannelItem {
    return ChannelItem(this, ChannelItem.Type.FOLDED)
}

fun List<Channel>.toChannelItem(): List<ChannelItem> {
    return map { it.toChannelItem() }
}