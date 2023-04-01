package com.spinoza.messenger_tfs.domain.repository

data class PresenceQueue(
    val queueId: String = "",
    var lastEventId: Long = DEFAULT_ID,
) {
    companion object {
        private const val DEFAULT_ID = -1L
    }
}