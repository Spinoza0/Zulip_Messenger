package com.spinoza.messenger_tfs.data

import com.spinoza.messenger_tfs.data.model.MessageDto
import com.spinoza.messenger_tfs.data.model.ReactionParamDto
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.ReactionParam
import java.util.*

fun MessageDto.toDomain(userId: Int): Message {
    return Message(
        date = this.date,
        userId = this.userId,
        name = this.name,
        content = this.content,
        avatarResId = this.avatarResId,
        reactions = this.reactions.toDomain(userId),
        isIconAddVisible = this.reactions.isNotEmpty(),
        id = this.id
    )
}

fun TreeSet<MessageDto>.toDomain(userId: Int): List<Message> {
    return this.map { it.toDomain(userId) }
}

private fun ReactionParamDto.toDomain(userId: Int): ReactionParam {
    return ReactionParam(this.usersIds.size, this.usersIds.contains(userId))
}

fun Message.toDto(userId: Int, messageId: Int): MessageDto {
    return MessageDto(
        date = this.date,
        userId = this.userId,
        name = this.name,
        content = this.content,
        avatarResId = this.avatarResId,
        reactions = this.reactions.toDto(userId),
        id = messageId
    )
}

private fun Map<String, ReactionParamDto>.toDomain(userId: Int): Map<String, ReactionParam> {
    return this.map { it.key to it.value.toDomain(userId) }.toMap()
}

private fun Map<String, ReactionParam>.toDto(userId: Int): Map<String, ReactionParamDto> {
    return this.map { it.key to ReactionParamDto(listOf(userId)) }.toMap()
}
