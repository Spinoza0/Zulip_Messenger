package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.cache.MessagesCache
import com.spinoza.messenger_tfs.data.network.apiservice.ZulipApiService
import com.spinoza.messenger_tfs.data.network.apiservice.ZulipApiService.Companion.RESULT_SUCCESS
import com.spinoza.messenger_tfs.data.network.model.event.DeleteMessageEventsResponse
import com.spinoza.messenger_tfs.data.network.model.event.HeartBeatEventsResponse
import com.spinoza.messenger_tfs.data.network.model.event.MessageEventsResponse
import com.spinoza.messenger_tfs.data.network.model.event.PresenceEventsResponse
import com.spinoza.messenger_tfs.data.network.model.event.ReactionEventsResponse
import com.spinoza.messenger_tfs.data.network.model.event.RegisterEventQueueResponse
import com.spinoza.messenger_tfs.data.network.model.event.StreamEventsResponse
import com.spinoza.messenger_tfs.data.network.model.event.SubscriptionEventsResponse
import com.spinoza.messenger_tfs.data.network.model.event.UpdateMessageEventsResponse
import com.spinoza.messenger_tfs.data.utils.apiRequest
import com.spinoza.messenger_tfs.data.utils.createNarrowJsonForEvents
import com.spinoza.messenger_tfs.data.utils.getBodyOrThrow
import com.spinoza.messenger_tfs.data.utils.listToDomain
import com.spinoza.messenger_tfs.data.utils.runCatchingNonCancellation
import com.spinoza.messenger_tfs.data.utils.toDomain
import com.spinoza.messenger_tfs.data.utils.toStringsList
import com.spinoza.messenger_tfs.di.DispatcherIO
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesResult
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class EventsRepositoryImpl @Inject constructor(
    private val messagesCache: MessagesCache,
    private val apiService: ZulipApiService,
    private val jsonConverter: Json,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) : EventsRepository {

    override suspend fun registerEventQueue(
        eventTypes: List<EventType>,
        filter: MessagesFilter,
    ): Result<EventsQueue> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val response = apiRequest<RegisterEventQueueResponse> {
                apiService.registerEventQueue(
                    narrow = filter.createNarrowJsonForEvents(),
                    eventTypes = Json.encodeToString(eventTypes.toStringsList())
                )
            }
            EventsQueue(response.queueId, response.lastEventId, eventTypes)
        }
    }

    override suspend fun deleteEventQueue(queueId: String): Unit = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            apiService.deleteEventQueue(queueId)
        }
    }

    override suspend fun getPresenceEvents(
        queue: EventsQueue,
    ): Result<List<PresenceEvent>> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val eventResponseBody = getNonHeartBeatEventResponse(queue)
            val eventResponse = jsonConverter.decodeFromString(
                PresenceEventsResponse.serializer(), eventResponseBody
            )
            if (eventResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(eventResponse.msg)
            }
            eventResponse.events.toDomain()
        }
    }

    override suspend fun getChannelEvents(
        queue: EventsQueue,
        channelsFilter: ChannelsFilter,
    ): Result<List<ChannelEvent>> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val eventResponseBody = getNonHeartBeatEventResponse(queue)
            val eventResponse = jsonConverter.decodeFromString(
                StreamEventsResponse.serializer(), eventResponseBody
            )
            if (eventResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(eventResponse.msg)
            }
            eventResponse.events.toDomain(channelsFilter)
        }
    }

    override suspend fun getChannelSubscriptionEvents(
        queue: EventsQueue,
        channelsFilter: ChannelsFilter,
    ): Result<List<ChannelEvent>> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val eventResponseBody = getNonHeartBeatEventResponse(queue)
            val eventResponse = jsonConverter.decodeFromString(
                SubscriptionEventsResponse.serializer(), eventResponseBody
            )
            if (eventResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(eventResponse.msg)
            }
            eventResponse.events.listToDomain(channelsFilter)
        }
    }

    override suspend fun getMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<MessageEvent> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val responseBody = getNonHeartBeatEventResponse(queue)
            val eventResponse = jsonConverter.decodeFromString(
                MessageEventsResponse.serializer(), responseBody
            )
            if (eventResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(eventResponse.msg)
            }
            var lastEventId = queue.lastEventId
            if (messagesCache.isNotEmpty() &&
                filter.topic.lastMessageId == messagesCache.getLastMessageId(filter)
            ) {
                eventResponse.events.forEach { messageEventDto ->
                    messagesCache.add(messageEventDto.message, isLastMessageVisible, filter)
                    lastEventId = messageEventDto.id
                }
            }
            val messages = messagesCache.getMessages(filter)
            MessageEvent(
                lastEventId,
                MessagesResult(messages, MessagePosition(), eventResponse.events.isNotEmpty())
            )
        }
    }

    override suspend fun getUpdateMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<UpdateMessageEvent> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val responseBody = getNonHeartBeatEventResponse(queue)
            val eventResponse = jsonConverter.decodeFromString(
                UpdateMessageEventsResponse.serializer(), responseBody
            )
            if (eventResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(eventResponse.msg)
            }
            var lastEventId = queue.lastEventId
            eventResponse.events.forEach { updateMessageEventDto ->
                messagesCache.update(
                    updateMessageEventDto.messageId,
                    updateMessageEventDto.subject,
                    updateMessageEventDto.renderedContent
                )
                lastEventId = updateMessageEventDto.id
            }
            val messages = messagesCache.getMessages(filter)
            UpdateMessageEvent(lastEventId, MessagesResult(messages, MessagePosition()))
        }
    }

    override suspend fun getDeleteMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<DeleteMessageEvent> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val responseBody = getNonHeartBeatEventResponse(queue)
            val eventResponse = jsonConverter.decodeFromString(
                DeleteMessageEventsResponse.serializer(), responseBody
            )
            if (eventResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(eventResponse.msg)
            }
            var lastEventId = queue.lastEventId
            eventResponse.events.forEach { deleteMessageEventDto ->
                messagesCache.remove(deleteMessageEventDto.messageId)
                lastEventId = deleteMessageEventDto.id
            }
            val messages = messagesCache.getMessages(filter)
            DeleteMessageEvent(lastEventId, MessagesResult(messages, MessagePosition()))
        }
    }

    override suspend fun getReactionEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<ReactionEvent> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val responseBody = getNonHeartBeatEventResponse(queue)
            val eventResponse = jsonConverter.decodeFromString(
                ReactionEventsResponse.serializer(), responseBody
            )
            if (eventResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(eventResponse.msg)
            }
            var lastEventId = queue.lastEventId
            eventResponse.events.forEach { reactionEventDto ->
                messagesCache.updateReaction(reactionEventDto)
                lastEventId = reactionEventDto.id
            }
            val messages = messagesCache.getMessages(filter)
            ReactionEvent(lastEventId, MessagesResult(messages, MessagePosition()))
        }
    }

    private suspend fun getNonHeartBeatEventResponse(queue: EventsQueue): String {
        var lastEventId = queue.lastEventId
        var isHeartBeat = true
        var responseBody: String
        do {
            val response = apiService.getEventsFromQueue(queue.queueId, lastEventId)
            if (!response.isSuccessful) {
                throw RepositoryError(response.message())
            }
            responseBody = response.getBodyOrThrow().string()
            val heartBeatEventsResponse = jsonConverter.decodeFromString(
                HeartBeatEventsResponse.serializer(), responseBody
            )
            if (heartBeatEventsResponse.result != RESULT_SUCCESS) {
                throw RepositoryError(heartBeatEventsResponse.msg)
            }
            heartBeatEventsResponse.events.forEach { heartBeatEventDto ->
                lastEventId = heartBeatEventDto.id
                isHeartBeat = heartBeatEventDto.type == ZulipApiService.EVENT_HEARTBEAT
            }
            if (isHeartBeat) {
                delay(DELAY_BEFORE_GET_NEXT_HEARTBEAT)
            }
        } while (isHeartBeat)
        return responseBody
    }

    companion object {

        private const val DELAY_BEFORE_GET_NEXT_HEARTBEAT = 1_000L
    }
}