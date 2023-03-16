package com.spinoza.messenger_tfs.domain.model

data class MessageDate(
    val date: String,
) : Comparable<MessageDate> {
    override fun compareTo(other: MessageDate): Int {
        return date.compareTo(other.date)
    }
}