package com.spinoza.messenger_tfs.domain.model

data class MessageDate(
    val value: String,
    val timestamp: Long,
) : Comparable<MessageDate> {

    override fun compareTo(other: MessageDate): Int {
        return timestamp.compareTo(other.timestamp)
    }
}