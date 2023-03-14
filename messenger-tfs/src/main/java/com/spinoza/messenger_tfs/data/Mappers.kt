package com.spinoza.messenger_tfs.data

import com.spinoza.messenger_tfs.data.model.MessageDto
import com.spinoza.messenger_tfs.data.model.ReactionParamDto
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.ReactionParam
import java.util.*

fun MessageDto.toEntity(userId: Int): Message {
    return Message(
        date = this.date,
        userId = this.userId,
        name = this.name,
        text = this.text,
        avatarResId = this.avatarResId,
        reactions = this.reactions.toEntity(userId),
        isIconAddVisible = this.reactions.isNotEmpty(),
        id = this.id
    )
}

fun TreeSet<MessageDto>.toEntity(userId: Int): List<Message> {
    return this.map { it.toEntity(userId) }
}

private fun ReactionParamDto.toEntity(userId: Int): ReactionParam {
    return ReactionParam(this.usersIds.size, this.usersIds.contains(userId))
}

fun Message.toDto(userId: Int, messageId: Int): MessageDto {
    return MessageDto(
        date = this.date,
        userId = this.userId,
        name = this.name,
        text = this.text,
        avatarResId = this.avatarResId,
        reactions = this.reactions.toDto(userId),
        id = messageId
    )
}

private fun Map<String, ReactionParamDto>.toEntity(userId: Int): Map<String, ReactionParam> {
    return this.map { it.key to it.value.toEntity(userId) }.toMap()
}

private fun Map<String, ReactionParam>.toDto(userId: Int): Map<String, ReactionParamDto> {
    return this.map { it.key to ReactionParamDto(listOf(userId)) }.toMap()
}
