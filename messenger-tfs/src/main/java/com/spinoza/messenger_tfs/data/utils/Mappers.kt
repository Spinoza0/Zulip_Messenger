package com.spinoza.messenger_tfs.data.utils

import com.spinoza.messenger_tfs.data.database.model.MessageDataDbModel
import com.spinoza.messenger_tfs.data.database.model.MessageDbModel
import com.spinoza.messenger_tfs.data.database.model.ReactionDbModel
import com.spinoza.messenger_tfs.data.database.model.StreamDbModel
import com.spinoza.messenger_tfs.data.database.model.TopicDbModel
import com.spinoza.messenger_tfs.data.network.model.event.EventTypeDto
import com.spinoza.messenger_tfs.data.network.model.event.PresenceEventDto
import com.spinoza.messenger_tfs.data.network.model.event.ReactionEventDto
import com.spinoza.messenger_tfs.data.network.model.event.StreamEventDto
import com.spinoza.messenger_tfs.data.network.model.event.SubscriptionEventDto
import com.spinoza.messenger_tfs.data.network.model.message.MessageDto
import com.spinoza.messenger_tfs.data.network.model.message.ReactionDto
import com.spinoza.messenger_tfs.data.network.model.presence.PresenceDto
import com.spinoza.messenger_tfs.data.network.model.presence.PresenceTypeDto
import com.spinoza.messenger_tfs.data.network.model.stream.StreamDto
import com.spinoza.messenger_tfs.data.network.model.stream.TopicDto
import com.spinoza.messenger_tfs.data.network.model.user.OwnUserResponse
import com.spinoza.messenger_tfs.data.network.model.user.UserDto
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.domain.model.ReactionParam
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.model.event.ChannelEvent
import com.spinoza.messenger_tfs.domain.model.event.EventOperation
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.PresenceEvent
import com.spinoza.messenger_tfs.domain.util.EMPTY_STRING
import com.spinoza.messenger_tfs.domain.util.MILLIS_IN_SECOND
import com.spinoza.messenger_tfs.domain.util.isContainingWords
import com.spinoza.messenger_tfs.domain.util.splitToWords
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TreeSet

fun TreeSet<MessageDto>.toDbModel(): List<MessageDbModel> {
    return map { it.toDbModel() }
}

fun MessageDto.toDbModel(): MessageDbModel {
    return MessageDbModel(this.toDataDbModel(), this.reactions.toDbModel(this.id))
}

fun List<MessageDbModel>.dbModelToDto(): List<MessageDto> {
    return map { it.dbModelToDto() }
}

fun List<StreamDto>.dtoToDomain(channelsFilter: ChannelsFilter): List<Channel> {
    val words = channelsFilter.name.splitToWords()
    return filter { it.name.isContainingWords(words) }
        .map { it.dtoToDomain(channelsFilter) }
}

fun List<StreamDbModel>.dbToDomain(channelsFilter: ChannelsFilter): List<Channel> {
    val words = channelsFilter.name.splitToWords()
    return filter { it.isSubscribed == channelsFilter.isSubscribed }
        .filter { it.name.isContainingWords(words) }
        .map { it.dbToDomain(channelsFilter) }
}

fun List<StreamDto>.toDbModel(channelsFilter: ChannelsFilter): List<StreamDbModel> {
    return map { it.toDbModel(channelsFilter) }
}

fun MessageDto.toDomain(userId: Long): Message {
    val dateTimestamp = timestamp.getDateFromTimestamp()
    return Message(
        date = MessageDate(dateTimestamp.unixTimeToString(), dateTimestamp, timestamp),
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

fun Collection<MessageDto>.toDomain(userId: Long): List<Message> {
    return filter {
        !it.isMeMessage
    }.map { it.toDomain(userId) }
}

fun UserDto.toDomain(presence: User.Presence): User {
    return User(
        userId = userId,
        email = email,
        fullName = fullName,
        avatarUrl = avatarUrl ?: EMPTY_STRING,
        presence = presence
    )
}

fun OwnUserResponse.toDomain(presence: User.Presence): User {
    return User(
        email = email,
        userId = userId,
        fullName = fullName,
        avatarUrl = avatarUrl ?: EMPTY_STRING,
        presence = presence
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

fun List<StreamEventDto>.toDomain(channelsFilter: ChannelsFilter): List<ChannelEvent> {
    val events = mutableListOf<ChannelEvent>()
    map { streamEventDto ->
        streamEventDto.streams.forEach { streamDto ->
            events.add(streamEventDto.toDomain(streamDto, channelsFilter))
        }
    }
    return events
}

fun List<SubscriptionEventDto>.listToDomain(channelsFilter: ChannelsFilter): List<ChannelEvent> {
    val events = mutableListOf<ChannelEvent>()
    map { subscriptionEventDto ->
        subscriptionEventDto.streams.forEach { streamDto ->
            events.add(subscriptionEventDto.dtoToDomain(streamDto, channelsFilter))
        }
    }
    return events
}

fun List<TopicDto>.toDbModel(channel: Channel): List<TopicDbModel> {
    return map { it.toDbModel(channel) }
}

fun List<TopicDto>.dtoToDomain(channel: Channel): List<Topic> {
    return map { it.dtoToDomain(channel) }
}

fun List<TopicDbModel>.dbToDomain(): List<Topic> {
    return map { it.dbToDomain() }
}

private fun TopicDbModel.dbToDomain(): Topic {
    return Topic(
        name = name,
        messageCount = Topic.NO_MESSAGES,
        channelId = streamId,
        lastMessageId = Message.UNDEFINED_ID
    )
}

fun TopicDto.dtoToDomain(channel: Channel): Topic {
    return Topic(
        name = name,
        messageCount = Topic.NO_MESSAGES,
        channelId = channel.channelId,
        lastMessageId = maxId
    )
}

private fun TopicDto.toDbModel(channel: Channel): TopicDbModel {
    return TopicDbModel(
        name = name,
        streamId = channel.channelId,
        isSubscribed = channel.isSubscribed
    )
}

private fun EventType.toDto(): EventTypeDto = when (this) {
    EventType.PRESENCE -> EventTypeDto.PRESENCE
    EventType.CHANNEL -> EventTypeDto.STREAM
    EventType.CHANNEL_SUBSCRIPTION -> EventTypeDto.CHANNEL_SUBSCRIPTION
    EventType.MESSAGE -> EventTypeDto.MESSAGE
    EventType.UPDATE_MESSAGE -> EventTypeDto.UPDATE_MESSAGE
    EventType.DELETE_MESSAGE -> EventTypeDto.DELETE_MESSAGE
    EventType.REACTION -> EventTypeDto.REACTION
}

private fun StreamEventDto.toDomain(
    streamDto: StreamDto,
    channelsFilter: ChannelsFilter,
): ChannelEvent {
    val operation =
        if (operation == EventOperation.DELETE.value) EventOperation.DELETE
        else EventOperation.CREATE
    return ChannelEvent(id, operation, streamDto.dtoToDomain(channelsFilter))
}

private fun SubscriptionEventDto.dtoToDomain(
    streamDto: StreamDto,
    channelsFilter: ChannelsFilter,
): ChannelEvent {
    val operation =
        if (operation == EventOperation.REMOVE.value) EventOperation.REMOVE
        else EventOperation.ADD
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

private fun MessageDbModel.dbModelToDto(): MessageDto {
    return MessageDto(
        id = message.id,
        streamId = message.streamId,
        senderId = message.senderId,
        content = message.content,
        recipientId = message.recipientId,
        timestamp = message.timestamp,
        subject = message.subject,
        isMeMessage = message.isMeMessage,
        reactions = reactions.toDto(),
        senderFullName = message.senderFullName,
        senderEmail = message.senderEmail,
        avatarUrl = message.avatarUrl
    )
}

private fun List<ReactionDbModel>.toDto(): List<ReactionDto> {
    return map { it.toDto() }
}

private fun ReactionDbModel.toDto(): ReactionDto {
    return ReactionDto(
        emojiName = emojiName,
        emojiCode = emojiCode,
        reactionType = reactionType,
        userId = userId
    )
}

private fun MessageDto.toDataDbModel(): MessageDataDbModel {
    return MessageDataDbModel(
        id = id,
        streamId = streamId,
        senderId = senderId,
        content = content,
        recipientId = recipientId,
        timestamp = timestamp,
        subject = subject,
        isMeMessage = isMeMessage,
        senderFullName = senderFullName,
        senderEmail = senderEmail,
        avatarUrl = avatarUrl ?: EMPTY_STRING
    )
}

private fun List<ReactionDto>.toDbModel(messageId: Long): List<ReactionDbModel> {
    return map { it.toDbModel(messageId) }
}

private fun ReactionDto.toDbModel(messageId: Long): ReactionDbModel {
    return ReactionDbModel(
        emojiName = emojiName,
        emojiCode = emojiCode,
        reactionType = reactionType,
        userId = userId,
        messageId = messageId
    )
}

private fun StreamDto.toDbModel(channelsFilter: ChannelsFilter): StreamDbModel {
    return StreamDbModel(
        streamId = streamId,
        name = name,
        isSubscribed = channelsFilter.isSubscribed
    )
}

private fun Long.unixTimeToString(): String {
    return SimpleDateFormat(
        DATE_FORMAT,
        Locale.getDefault()
    ).format(Date(this * MILLIS_IN_SECOND))
}

private fun Long.getDateFromTimestamp(): Long {
    return this - (this % SECONDS_IN_DAY)
}

private const val DATE_FORMAT = "dd.MM.yyyy"
private const val SECONDS_IN_DAY = 24 * 60 * 60