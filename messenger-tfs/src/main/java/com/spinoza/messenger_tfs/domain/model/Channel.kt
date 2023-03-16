package com.spinoza.messenger_tfs.domain.model

data class Channel(
    val streamId: Long,
    val name: String,
    val topics: List<Topic>,
    val type: Type = Type.FOLDED,
) {

    enum class Type { FOLDED, UNFOLDED }
}