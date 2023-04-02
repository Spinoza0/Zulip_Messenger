package com.spinoza.messenger_tfs.data

import com.spinoza.messenger_tfs.data.model.event.PresenceEventDto
import com.spinoza.messenger_tfs.data.model.event.StreamEventDto
import com.spinoza.messenger_tfs.data.model.message.MessageDto
import com.spinoza.messenger_tfs.data.model.message.ReactionDto
import com.spinoza.messenger_tfs.data.model.presence.PresenceDto
import com.spinoza.messenger_tfs.data.model.stream.StreamDto
import com.spinoza.messenger_tfs.data.model.stream.TopicDto
import com.spinoza.messenger_tfs.data.model.user.OwnUserResponse
import com.spinoza.messenger_tfs.data.model.user.UserDto
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.model.event.ChannelEvent
import com.spinoza.messenger_tfs.domain.model.event.PresenceEvent
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
    val strippedTimestamp = timestamp.stripTimeFromTimestamp()
    return Message(
        date = MessageDate(strippedTimestamp.unixTimeToString(), strippedTimestamp),
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

fun OwnUserResponse.toUserDto(): UserDto {
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
    PRESENCE_ACTIVE -> User.Presence.ACTIVE
    PRESENCE_IDLE -> User.Presence.IDLE
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

fun List<PresenceEventDto>.toDomain(): List<PresenceEvent> {
    return map { it.toDomain() }
}

fun List<StreamEventDto>.listToDomain(): List<ChannelEvent> {
    val events = mutableListOf<ChannelEvent>()
    filter { it.operation != OPERATION_DELETE }
        .map { streamEventDto ->
            streamEventDto.streams.forEach { events.add(streamEventDto.toDomain(it)) }
        }
    return events
}

private fun StreamEventDto.toDomain(streamDto: StreamDto): ChannelEvent {
    return ChannelEvent(id, streamDto.toDomain())
}

private fun PresenceEventDto.toDomain(): PresenceEvent {
    var presenceValue = User.Presence.OFFLINE
    presence.values.forEach { value ->
        if (value.status == PRESENCE_IDLE && presenceValue.ordinal > User.Presence.IDLE.ordinal) {
            presenceValue = User.Presence.IDLE
        } else if (value.status == PRESENCE_ACTIVE) {
            presenceValue = User.Presence.ACTIVE
        }
    }
    return PresenceEvent(
        id = id,
        userId = userId,
        email = email,
        presence = presenceValue
    )
}

private fun Long.unixTimeToString(): String {
    return SimpleDateFormat(
        DATE_FORMAT,
        Locale.getDefault()
    ).format(Date(this * MILLIS_IN_SECOND))
}

private fun Long.stripTimeFromTimestamp(): Long {
    return this - (this % SECONDS_IN_DAY)
}

private fun StreamDto.toDomain(): Channel {
    return Channel(
        channelId = streamId,
        name = name
    )
}

private const val DATE_FORMAT = "dd.MM.yyyy"
private const val PRESENCE_ACTIVE = "active"
private const val PRESENCE_IDLE = "idle"
private const val OPERATION_DELETE = "delete"
private const val MILLIS_IN_SECOND = 1000L
private const val SECONDS_IN_DAY = 24 * 60 * 60