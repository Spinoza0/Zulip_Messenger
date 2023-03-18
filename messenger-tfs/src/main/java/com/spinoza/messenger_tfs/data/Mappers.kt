package com.spinoza.messenger_tfs.data

import com.spinoza.messenger_tfs.data.model.ChannelDto
import com.spinoza.messenger_tfs.data.model.MessageDto
import com.spinoza.messenger_tfs.data.model.ReactionParamDto
import com.spinoza.messenger_tfs.data.model.TopicDto
import com.spinoza.messenger_tfs.domain.model.*
import java.util.*

fun List<TopicDto>.toDomain(messages: TreeSet<MessageDto>, channelId: Long): List<Topic> {
    return this.map { it.toDomain(messages, channelId) }
}

fun List<ChannelDto>.toDomain(): List<Channel> {
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
        it.channelId == channelFilter.channelId && it.topicName == channelFilter.topicName
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

private fun ChannelDto.toDomain(): Channel {
    return Channel(
        channelId = this.id,
        name = this.name
    )
}

private fun TopicDto.toDomain(messages: TreeSet<MessageDto>, channelId: Long): Topic {
    return Topic(
        name = this.name,
        messageCount = messages.count { it.channelId == channelId && it.topicName == this.name }
    )
}