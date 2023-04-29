package com.spinoza.messenger_tfs.domain.model.event

data class EventsQueue(
    val queueId: String = "",
    val lastEventId: Long = DEFAULT_ID,
    val eventTypes: List<EventType> = emptyList(),
) {
    companion object {
        private const val DEFAULT_ID = -1L
    }
}