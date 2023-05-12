package com.spinoza.messenger_tfs.stub

import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.RepositoryError
import com.spinoza.messenger_tfs.domain.model.event.ChannelEvent
import com.spinoza.messenger_tfs.domain.model.event.DeleteMessageEvent
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.model.event.MessageEvent
import com.spinoza.messenger_tfs.domain.model.event.PresenceEvent
import com.spinoza.messenger_tfs.domain.model.event.ReactionEvent
import com.spinoza.messenger_tfs.domain.model.event.UpdateMessageEvent
import com.spinoza.messenger_tfs.domain.repository.EventsRepository

class EventsRepositoryStub : EventsRepository {

    override suspend fun registerEventQueue(
        eventTypes: List<EventType>,
        messagesFilter: MessagesFilter,
    ): Result<EventsQueue> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun deleteEventQueue(queueId: String) {}

    override suspend fun getPresenceEvents(queue: EventsQueue): Result<List<PresenceEvent>> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getChannelEvents(
        queue: EventsQueue,
        channelsFilter: ChannelsFilter,
    ): Result<List<ChannelEvent>> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getChannelSubscriptionEvents(
        queue: EventsQueue,
        channelsFilter: ChannelsFilter,
    ): Result<List<ChannelEvent>> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<MessageEvent> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getUpdateMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<UpdateMessageEvent> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getDeleteMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<DeleteMessageEvent> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getReactionEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<ReactionEvent> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    private companion object {

        const val ERROR_MSG = "Repository error"
    }
}