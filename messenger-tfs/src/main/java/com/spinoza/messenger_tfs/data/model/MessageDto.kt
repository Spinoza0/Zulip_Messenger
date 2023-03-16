package com.spinoza.messenger_tfs.data.model

import com.spinoza.messenger_tfs.domain.model.MessageDate

data class MessageDto(
    val date: MessageDate,
    val userId: Int,
    val name: String,
    val text: String,
    val avatarResId: Int,
    val reactions: Map<String, ReactionParamDto>,
    val id: Int,
) : Comparable<MessageDto> {
    override fun compareTo(other: MessageDto): Int {
        return id.compareTo(other.id)
    }
}