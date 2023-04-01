package com.spinoza.messenger_tfs.data

import com.spinoza.messenger_tfs.data.model.message.MessageDto
import com.spinoza.messenger_tfs.data.model.message.ReactionDto
import com.spinoza.messenger_tfs.data.model.presence.PresenceDto
import com.spinoza.messenger_tfs.data.model.stream.StreamDto
import com.spinoza.messenger_tfs.data.model.stream.TopicDto
import com.spinoza.messenger_tfs.data.model.user.OwnResponseDto
import com.spinoza.messenger_tfs.data.model.user.UserDto
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.repository.MessagesResult
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import java.text.SimpleDateFormat
import java.util.*

fun List<StreamDto>.toDomain(channelsFilter: ChannelsFilter): List<Channel> {
    return filter { subscribedStreamDto ->
        channelsFilter.name.split(" ").all { word ->
            subscribedStreamDto.name.contains(word, true)
        }
    }.map { it.toDomain() }
}

fun MessageDto.toDomain(userId: Long): Message {
    return Message(
        date = MessageDate(timestamp.unixTimeToString()),
        user = User(
            userId = senderId,
            email = senderEmail,
            fullName = senderFullName,
            avatarUrl = avatarUrl ?: "",
            presence = User.Presence.OFFLINE
        ),
        content = content,
        subject = subject,
        reactions = reactions.toDomain(userId),
        id = id
    )
}

fun List<ReactionDto>.toDomain(userId: Long): Map<Emoji, ReactionParam> {
    return associate { reactionDto ->
        Emoji(reactionDto.emoji_name, reactionDto.emoji_code) to ReactionParam(
            this.count { it.emoji_code == reactionDto.emoji_code },
            reactionDto.user_id == userId
        )
    }
}

fun List<MessageDto>.toDomain(userId: Long): List<Message> {
    return filter {
        !it.isMeMessage
    }.map { it.toDomain(userId) }
}

fun UserDto.toDomain(presence: User.Presence): User {
    return User(
        userId = userId,
        email = email,
        fullName = fullName,
        avatarUrl = avatarUrl ?: "",
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

fun List<TopicDto>.toDomain(messagesResult: RepositoryResult<MessagesResult>): List<Topic> {
    return if (messagesResult is RepositoryResult.Success) {
        map { topicDto ->
            Topic(
                topicDto.name,
                messagesResult.value.messages.count { it.subject == topicDto.name }
            )
        }
    } else {
        map { Topic(it.name, 0) }
    }
}

private fun Long.unixTimeToString(): String {
    return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(this * 1000))
}

private fun StreamDto.toDomain(): Channel {
    return Channel(
        channelId = streamId,
        name = name
    )
}