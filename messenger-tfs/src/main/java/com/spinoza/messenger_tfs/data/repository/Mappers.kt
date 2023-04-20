package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.database.model.StreamDbModel
import com.spinoza.messenger_tfs.data.network.model.event.EventTypeDto
import com.spinoza.messenger_tfs.data.network.model.event.PresenceEventDto
import com.spinoza.messenger_tfs.data.network.model.event.ReactionEventDto
import com.spinoza.messenger_tfs.data.network.model.event.StreamEventDto
import com.spinoza.messenger_tfs.data.network.model.message.MessageDto
import com.spinoza.messenger_tfs.data.network.model.message.ReactionDto
import com.spinoza.messenger_tfs.data.network.model.presence.PresenceDto
import com.spinoza.messenger_tfs.data.network.model.presence.PresenceTypeDto
import com.spinoza.messenger_tfs.data.network.model.stream.StreamDto
import com.spinoza.messenger_tfs.data.network.model.user.OwnUserResponse
import com.spinoza.messenger_tfs.data.network.model.user.UserDto
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.model.event.ChannelEvent
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.PresenceEvent
import java.text.SimpleDateFormat
import java.util.*

fun List<StreamDto>.dtoToDomain(channelsFilter: ChannelsFilter): List<Channel> {
    return filter { it.name.isContainsWords(channelsFilter.name) }
        .map { it.dtoToDomain(channelsFilter) }
}

fun List<StreamDbModel>.dbToDomain(channelsFilter: ChannelsFilter): List<Channel> {
    return filter { it.isSubscribed == channelsFilter.isSubscribed }
        .filter { it.name.isContainsWords(channelsFilter.name) }
        .map { it.dbToDomain(channelsFilter) }
}

fun List<StreamDto>.toDbModel(channelsFilter: ChannelsFilter): List<StreamDbModel> {
    return map { it.toDbModel(channelsFilter) }
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
        Emoji(reactionDto.emojiName, reactionDto.emojiCode) to ReactionParam(
            this.count { it.emojiCode == reactionDto.emojiCode },
            reactionDto.userId == userId
        )
    }
}

fun Emoji.toDto(userId: Long): ReactionDto {
    return ReactionDto(
        emojiName = name,
        emojiCode = code,
        reactionType = ReactionDto.REACTION_TYPE_UNICODE_EMOJI,
        userId = userId
    )
}

fun ReactionEventDto.toReactionDto(): ReactionDto {
    return ReactionDto(
        emoji_name,
        emoji_code,
        reaction_type,
        userId
    )
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
    PresenceTypeDto.ACTIVE.value -> User.Presence.ACTIVE
    PresenceTypeDto.IDLE.value -> User.Presence.IDLE
    PresenceTypeDto.OFFLINE.value -> User.Presence.OFFLINE
    else -> throw RuntimeException("Unknown presence status ${aggregated.status}")
}

fun List<PresenceEventDto>.toDomain(): List<PresenceEvent> {
    return map { it.toDomain() }
}

fun List<EventType>.toStringsList(): List<String> {
    return map { it.toDto().value }
}

fun List<StreamEventDto>.listToDomain(channelsFilter: ChannelsFilter): List<ChannelEvent> {
    val events = mutableListOf<ChannelEvent>()
    map { streamEventDto ->
        streamEventDto.streams.forEach { streamDto ->
            events.add(streamEventDto.toDomain(streamDto, channelsFilter))
        }
    }
    return events
}

private fun EventType.toDto(): EventTypeDto = when (this) {
    EventType.PRESENCE -> EventTypeDto.PRESENCE
    EventType.CHANNEL -> EventTypeDto.STREAM
    EventType.MESSAGE -> EventTypeDto.MESSAGE
    EventType.DELETE_MESSAGE -> EventTypeDto.DELETE_MESSAGE
    EventType.REACTION -> EventTypeDto.REACTION
}

private fun StreamEventDto.toDomain(
    streamDto: StreamDto,
    channelsFilter: ChannelsFilter,
): ChannelEvent {
    val operation =
        if (operation == ChannelEvent.Operation.DELETE.value) ChannelEvent.Operation.DELETE
        else ChannelEvent.Operation.CREATE
    return ChannelEvent(id, operation, streamDto.dtoToDomain(channelsFilter))
}

private fun PresenceEventDto.toDomain(): PresenceEvent {
    var presenceValue = User.Presence.OFFLINE
    presence.values.forEach { value ->
        if (value.status == PresenceTypeDto.IDLE.value &&
            presenceValue.ordinal > User.Presence.IDLE.ordinal
        ) {
            presenceValue = User.Presence.IDLE
        } else if (value.status == PresenceTypeDto.ACTIVE.value) {
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

private fun StreamDto.dtoToDomain(channelsFilter: ChannelsFilter): Channel {
    return Channel(
        channelId = streamId,
        name = name,
        isSubscribed = channelsFilter.isSubscribed
    )
}

private fun StreamDbModel.dbToDomain(channelsFilter: ChannelsFilter): Channel {
    return Channel(
        channelId = streamId,
        name = name,
        isSubscribed = channelsFilter.isSubscribed
    )
}

private fun StreamDto.toDbModel(channelsFilter: ChannelsFilter): StreamDbModel {
    return StreamDbModel(
        streamId = streamId,
        name = name,
        isSubscribed = channelsFilter.isSubscribed
    )
}

private fun String.isContainsWords(words: String): Boolean {
    return words.split(" ").all { word ->
        this.contains(word, true)
    }
}

private const val DATE_FORMAT = "dd.MM.yyyy"
private const val MILLIS_IN_SECOND = 1000L
private const val SECONDS_IN_DAY = 24 * 60 * 60