package com.spinoza.messenger_tfs.data.model

import com.spinoza.messenger_tfs.domain.model.MessageDate

data class MessageDto(
    val id: Long,
    val date: MessageDate,
    val userId: Long,
    val name: String,
    val content: String,
    val avatarResId: Int,
    val reactions: Map<String, ReactionParamDto>,
    val channelId: Long,
    val topicName: String,
) : Comparable<MessageDto> {
    override fun compareTo(other: MessageDto): Int {
        return id.compareTo(other.id)
    }
}