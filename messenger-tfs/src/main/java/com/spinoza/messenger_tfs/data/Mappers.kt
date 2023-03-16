package com.spinoza.messenger_tfs.data

import com.spinoza.messenger_tfs.data.model.MessageDto
import com.spinoza.messenger_tfs.data.model.ReactionParamDto
import com.spinoza.messenger_tfs.data.model.StreamDto
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.ReactionParam
import java.util.*
import kotlin.random.Random

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

fun TreeSet<MessageDto>.toDomain(userId: Long): List<Message> {
    return this.map { it.toDomain(userId) }
}

fun Message.toDto(userId: Long, messageId: Long): MessageDto {

    // TODO: сделано в целях тестирования, убрать, заменить на реальные данные
    val stream = streamsDto[Random.nextInt(streamsDto.size)]
    val topic = stream.topics[Random.nextInt(stream.topics.size)]

    return MessageDto(
        date = this.date,
        userId = this.userId,
        name = this.name,
        content = this.content,
        avatarResId = this.avatarResId,
        reactions = this.reactions.toDto(userId),
        id = messageId,
        streamId = stream.id,
        subject = topic.name
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
        streamId = this.id,
        name = this.name,
        topics = this.topics
    )
}
