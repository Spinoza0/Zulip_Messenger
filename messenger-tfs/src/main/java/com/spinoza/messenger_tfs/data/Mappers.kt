package com.spinoza.messenger_tfs.data

import com.spinoza.messenger_tfs.data.model.MessageDto
import com.spinoza.messenger_tfs.data.model.ReactionParamDto
import com.spinoza.messenger_tfs.data.model.StreamDto
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelFilter
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.ReactionParam
import java.util.*

fun List<StreamDto>.toDomain(): List<Channel> {
    return this.map { it.toDomain() }
}

fun MessageDto.toDomain(userId: Long): Message {
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

fun TreeSet<MessageDto>.toDomain(userId: Long, channelFilter: ChannelFilter): List<Message> {
    return this.filter {
        it.channelId == channelFilter.id && it.topicName == channelFilter.topic
    }.map { it.toDomain(userId) }
}

fun Message.toDto(userId: Long, messageId: Long, channelId: Long, topicName: String): MessageDto {

    return MessageDto(
        date = this.date,
        userId = this.userId,
        name = this.name,
        content = this.content,
        avatarResId = this.avatarResId,
        reactions = this.reactions.toDto(userId),
        id = messageId,
        channelId = channelId,
        topicName = topicName
    )
}

private fun Map<String, ReactionParamDto>.toDomain(userId: Long): Map<String, ReactionParam> {
    return this.map { it.key to it.value.toDomain(userId) }.toMap()
}

private fun Map<String, ReactionParam>.toDto(userId: Long): Map<String, ReactionParamDto> {
    return this.map { it.key to ReactionParamDto(listOf(userId)) }.toMap()
}

private fun ReactionParamDto.toDomain(userId: Long): ReactionParam {
    return ReactionParam(this.usersIds.size, this.usersIds.contains(userId))
}

private fun StreamDto.toDomain(): Channel {
    return Channel(
        channelId = this.id,
        name = this.name
    )
}
