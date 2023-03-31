package com.spinoza.messenger_tfs.data

import com.spinoza.messenger_tfs.data.model.*
import com.spinoza.messenger_tfs.domain.model.*
import java.util.*

private fun User.toDto(): OldUserDto {
    return OldUserDto(
        userId = userId,
        email = email,
        full_name = full_name,
        avatar_url = avatar_url,
        presence = presence
    )
}

fun List<TopicDto>.toDomain(messages: TreeSet<MessageDto>, channelId: Long): List<Topic> {
    return map { it.toDomain(messages, channelId) }
}

fun List<StreamDto>.toDomain(channelsFilter: ChannelsFilter): List<Channel> {
    return filter { subscribedStreamDto ->
        channelsFilter.name.split(" ").all { word ->
            subscribedStreamDto.name.contains(word, true)
        }
    }.map { it.toDomain() }
}

fun MessageDto.toDomain(userId: Long): Message {
    return Message(
        date = date,
        user = user.toDomain(),
        content = content,
        reactions = reactions.toDomain(userId),
        isIconAddVisible = reactions.isNotEmpty(),
        id = id
    )
}

fun TreeSet<MessageDto>.toDomain(userId: Long, messagesFilter: MessagesFilter): List<Message> {
    return filter {
        it.channelId == messagesFilter.channel.channelId && it.topicName == messagesFilter.topic.name
    }.map { it.toDomain(userId) }
}

fun Message.toDto(userId: Long, messageId: Long, messagesFilter: MessagesFilter): MessageDto {
    return MessageDto(
        date = date,
        user = user.toDto(),
        content = content,
        reactions = reactions.toDto(userId),
        id = messageId,
        channelId = messagesFilter.channel.channelId,
        topicName = messagesFilter.topic.name
    )
}

fun OldUserDto.toDomain(): User {
    return User(
        userId = userId,
        email = email,
        full_name = full_name,
        avatar_url = avatar_url ?: "",
        presence = presence
    )
}

fun UserDto.toDomain(presence: User.Presence): User {
    return User(
        userId = userId,
        email = email,
        full_name = fullName,
        avatar_url = avatarUrl ?: "",
        presence = presence
    )
}

fun OwnResponseDto.toUserDto(): UserDto {
    return UserDto(
        email = email,
        userId = userId,
        avatarVersion = avatarVersion,
        isAdmin = isAdmin,
        isOwner = isOwner,
        isGuest = isGuest,
        isBillingAdmin = isBillingAdmin,
        role = role,
        isBot = isBot,
        fullName = fullName,
        timezone = timezone,
        isActive = isActive,
        dateJoined = dateJoined,
        avatarUrl = avatarUrl,
        deliveryEmail = deliveryEmail,
        profileData = profileData
    )
}

fun PresenceDto.toDomain(): User.Presence = when (aggregated.status) {
    "active" -> User.Presence.ONLINE
    "idle" -> User.Presence.IDLE
    else -> User.Presence.OFFLINE
}

fun TopicDto.toDomain(messages: TreeSet<MessageDto>, channelId: Long): Topic {
    return Topic(
        name = name,
        messageCount = messages.count { it.channelId == channelId && it.topicName == name }
    )
}

private fun Map<String, ReactionParamDto>.toDomain(userId: Long): Map<String, ReactionParam> {
    return map { it.key to it.value.toDomain(userId) }.toMap()
}

private fun Map<String, ReactionParam>.toDto(userId: Long): Map<String, ReactionParamDto> {
    return map { it.key to ReactionParamDto(listOf(userId)) }.toMap()
}

private fun ReactionParamDto.toDomain(userId: Long): ReactionParam {
    return ReactionParam(usersIds.size, usersIds.contains(userId))
}

private fun StreamDto.toDomain(): Channel {
    return Channel(
        channelId = streamId,
        name = name
    )
}