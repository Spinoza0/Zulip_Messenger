package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesPageType
import com.spinoza.messenger_tfs.domain.model.MessagesResult
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.model.event.ChannelEvent
import com.spinoza.messenger_tfs.domain.model.event.DeleteMessageEvent
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.model.event.MessageEvent
import com.spinoza.messenger_tfs.domain.model.event.PresenceEvent
import com.spinoza.messenger_tfs.domain.model.event.ReactionEvent

interface WebRepository {

    suspend fun getLoggedInUserId(email: String, password: String): Result<Long>

    suspend fun getOwnUser(): Result<User>

    suspend fun getUser(userId: Long): Result<User>

    suspend fun getAllUsers(): Result<List<User>>

    suspend fun getMessages(
        messagesPageType: MessagesPageType,
        filter: MessagesFilter,
    ): Result<MessagesResult>

    suspend fun getChannels(channelsFilter: ChannelsFilter): Result<List<Channel>>

    suspend fun getTopics(channel: Channel): Result<List<Topic>>

    suspend fun getTopic(filter: MessagesFilter): Result<Topic>

    suspend fun getUpdatedMessageFilter(filter: MessagesFilter): MessagesFilter

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

    suspend fun getMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<MessageEvent>

    suspend fun getDeleteMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<DeleteMessageEvent>

    suspend fun getReactionEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<ReactionEvent>

    suspend fun setOwnStatusActive()

    suspend fun setMessagesFlagToRead(messageIds: List<Long>)
}