package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.model.event.*

interface MessagesRepository {

    suspend fun getApiKey(storedApiKey: String, email: String, password: String): Result<String>

    suspend fun getOwnUserId(): Result<Long>

    suspend fun getOwnUser(): Result<User>

    suspend fun getUser(userId: Long): Result<User>

    suspend fun getUsersByFilter(usersFilter: String): Result<List<User>>

    suspend fun getMessages(filter: MessagesFilter): Result<MessagesResult>

    suspend fun getChannelsFromCache(channelsFilter: ChannelsFilter): Result<List<Channel>>

    suspend fun getChannels(channelsFilter: ChannelsFilter): Result<List<Channel>>

    suspend fun getTopics(channel: Channel): Result<List<Topic>>

    suspend fun getTopic(filter: MessagesFilter): Result<Topic>

    suspend fun sendMessage(content: String, filter: MessagesFilter): Result<Long>

    suspend fun updateReaction(
        messageId: Long,
        emoji: Emoji,
        filter: MessagesFilter,
    ): Result<MessagesResult>

    suspend fun registerEventQueue(
        eventTypes: List<EventType>,
        messagesFilter: MessagesFilter,
    ): Result<EventsQueue>

    suspend fun deleteEventQueue(queueId: String)

    suspend fun getPresenceEvents(queue: EventsQueue): Result<List<PresenceEvent>>

    suspend fun getChannelEvents(
        queue: EventsQueue,
        channelsFilter: ChannelsFilter,
    ): Result<List<ChannelEvent>>

    suspend fun getMessageEvent(queue: EventsQueue, filter: MessagesFilter): Result<MessageEvent>

    suspend fun getDeleteMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
    ): Result<DeleteMessageEvent>

    suspend fun getReactionEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
    ): Result<ReactionEvent>

    suspend fun setOwnStatusActive()

    suspend fun setMessagesFlagToRead(messageIds: List<Long>)
}