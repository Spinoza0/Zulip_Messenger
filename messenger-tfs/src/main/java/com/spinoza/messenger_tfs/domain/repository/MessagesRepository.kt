package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.model.event.*

interface MessagesRepository {

    suspend fun getOwnUserId(): RepositoryResult<Long>

    suspend fun getOwnUser(): RepositoryResult<User>

    suspend fun getUser(userId: Long): RepositoryResult<User>

    suspend fun getUsersByFilter(usersFilter: String): RepositoryResult<List<User>>

    suspend fun getMessages(
        filter: MessagesFilter,
        messageId: Long = Message.UNDEFINED_ID,
    ): RepositoryResult<MessagesResult>

    suspend fun getChannels(channelsFilter: ChannelsFilter): RepositoryResult<List<Channel>>

    suspend fun getTopics(channel: Channel): RepositoryResult<List<Topic>>

    suspend fun getTopic(filter: MessagesFilter): RepositoryResult<Topic>

    suspend fun sendMessage(
        content: String,
        filter: MessagesFilter,
    ): RepositoryResult<Long>

    suspend fun updateReaction(
        messageId: Long,
        emoji: Emoji,
        filter: MessagesFilter,
    ): RepositoryResult<MessagesResult>

    suspend fun registerEventQueue(eventTypes: List<EventType>): RepositoryResult<EventsQueue>

    suspend fun deleteEventQueue(queueId: String)

    suspend fun getPresenceEvents(queue: EventsQueue): RepositoryResult<List<PresenceEvent>>

    suspend fun getChannelEvents(queue: EventsQueue): RepositoryResult<List<ChannelEvent>>

    suspend fun getMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
    ): RepositoryResult<MessageEvent>

    suspend fun getDeleteMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
    ): RepositoryResult<DeleteMessageEvent>

    suspend fun getReactionEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
    ): RepositoryResult<ReactionEvent>

    suspend fun setOwnStatusActive()
}