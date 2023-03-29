package com.spinoza.messenger_tfs.data

import com.spinoza.messenger_tfs.data.model.*
import com.spinoza.messenger_tfs.domain.model.*
import java.util.*

private fun User.toDto(): UserDto {
    return UserDto(
        userId = this.userId,
        email = this.email,
        full_name = this.full_name,
        avatar_url = this.avatar_url,
        presence = this.presence
    )
}

fun List<UserDto>.listToDomain(usersFilter: String): List<User> {
    return this.filter {
        if (usersFilter.isBlank()) {
            true
        } else {
            it.full_name.contains(usersFilter, true)
        }
    }.map { it.toDomain() }
}

fun List<TopicDto>.toDomain(messages: TreeSet<MessageDto>, channelId: Long): List<Topic> {
    return this.map { it.toDomain(messages, channelId) }
}

fun List<ChannelDto>.toDomain(channelsFilter: ChannelsFilter): List<Channel> {
    return this
        .filter { channelsDto ->
            if (channelsFilter.subscriptionStatus) channelsDto.subscriptionStatus
            else true
        }
        .filter { channelsDto ->
            channelsFilter.name.split(" ").all { word ->
                channelsDto.name.contains(word, true)
            }
        }
        .map { it.toDomain() }
}

fun MessageDto.toDomain(userId: Long): Message {
    return Message(
        date = this.date,
        user = this.user.toDomain(),
        content = this.content,
        reactions = this.reactions.toDomain(userId),
        isIconAddVisible = this.reactions.isNotEmpty(),
        id = this.id
    )
}

fun TreeSet<MessageDto>.toDomain(userId: Long, messagesFilter: MessagesFilter): List<Message> {
    return this.filter {
        it.channelId == messagesFilter.channel.channelId && it.topicName == messagesFilter.topic.name
    }.map { it.toDomain(userId) }
}

fun Message.toDto(userId: Long, messageId: Long, messagesFilter: MessagesFilter): MessageDto {
    return MessageDto(
        date = this.date,
        user = this.user.toDto(),
        content = this.content,
        reactions = this.reactions.toDto(userId),
        id = messageId,
        channelId = messagesFilter.channel.channelId,
        topicName = messagesFilter.topic.name
    )
}

fun UserDto.toDomain(): User {
    return User(
        userId = this.userId,
        email = this.email,
        full_name = this.full_name,
        avatar_url = this.avatar_url ?: "",
        presence = this.presence
    )
}

fun TopicDto.toDomain(messages: TreeSet<MessageDto>, channelId: Long): Topic {
    return Topic(
        name = this.name,
        messageCount = messages.count { it.channelId == channelId && it.topicName == this.name }
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