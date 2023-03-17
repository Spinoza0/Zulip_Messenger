package com.spinoza.messenger_tfs.domain.model

data class Channel(
    val channelId: Long,
    val name: String,
    val type: Type = Type.FOLDED,
) {

    enum class Type { FOLDED, UNFOLDED }
}