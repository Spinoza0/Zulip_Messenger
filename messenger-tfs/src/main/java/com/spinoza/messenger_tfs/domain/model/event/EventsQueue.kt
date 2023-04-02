package com.spinoza.messenger_tfs.domain.model.event

data class EventsQueue(
    val queueId: String = "",
    val lastEventId: Long = DEFAULT_ID,
) {
    companion object {
        private const val DEFAULT_ID = -1L
    }
}