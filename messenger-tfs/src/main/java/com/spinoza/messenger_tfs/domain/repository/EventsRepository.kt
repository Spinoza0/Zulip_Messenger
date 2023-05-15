package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.event.ChannelEvent
import com.spinoza.messenger_tfs.domain.model.event.DeleteMessageEvent
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.model.event.MessageEvent
import com.spinoza.messenger_tfs.domain.model.event.PresenceEvent
import com.spinoza.messenger_tfs.domain.model.event.ReactionEvent
import com.spinoza.messenger_tfs.domain.model.event.UpdateMessageEvent

interface EventsRepository {

    suspend fun registerEventQueue(
        eventTypes: List<EventType>,
        filter: MessagesFilter,
    ): Result<EventsQueue>

    suspend fun deleteEventQueue(queueId: String)

    suspend fun getPresenceEvents(queue: EventsQueue): Result<List<PresenceEvent>>

    suspend fun getChannelEvents(
        queue: EventsQueue,
        channelsFilter: ChannelsFilter,
    ): Result<List<ChannelEvent>>

    suspend fun getChannelSubscriptionEvents(
        queue: EventsQueue,
        channelsFilter: ChannelsFilter,
    ): Result<List<ChannelEvent>>

    suspend fun getMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<MessageEvent>


    suspend fun getUpdateMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<UpdateMessageEvent>

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
}