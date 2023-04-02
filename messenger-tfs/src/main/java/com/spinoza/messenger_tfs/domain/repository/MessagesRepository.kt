package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.model.event.ChannelEvent
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.model.event.PresenceEvent

interface MessagesRepository {

    suspend fun getOwnUserId(): RepositoryResult<Long>

    suspend fun getOwnUser(): RepositoryResult<User>

    suspend fun getUser(userId: Long): RepositoryResult<User>

    suspend fun getUsersByFilter(usersFilter: String): RepositoryResult<List<User>>

    suspend fun getMessages(
        messagesFilter: MessagesFilter,
        messageId: Long = Message.UNDEFINED_ID,
    ): RepositoryResult<MessagesResult>

    suspend fun getChannels(channelsFilter: ChannelsFilter): RepositoryResult<List<Channel>>

    suspend fun getTopics(channel: Channel): RepositoryResult<List<Topic>>

    suspend fun getTopic(messagesFilter: MessagesFilter): RepositoryResult<Topic>

    suspend fun sendMessage(
        content: String,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<MessagesResult>

    suspend fun updateReaction(
        messageId: Long,
        emoji: Emoji,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<MessagesResult>

    suspend fun registerEventQueue(eventType: EventType): RepositoryResult<EventsQueue>

    suspend fun deleteEventQueue(queueId: String)

    suspend fun getPresenceEvents(queue: EventsQueue): RepositoryResult<List<PresenceEvent>>

    suspend fun getChannelEvents(queue: EventsQueue): RepositoryResult<List<ChannelEvent>>
}