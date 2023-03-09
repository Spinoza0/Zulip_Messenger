package com.spinoza.messenger_tfs.domain.model

data class MessageDate(
    val id: Int,
    val value: String,
) : Comparable<MessageDate> {
    override fun compareTo(other: MessageDate): Int {
        return value.compareTo(other.value)
    }
}