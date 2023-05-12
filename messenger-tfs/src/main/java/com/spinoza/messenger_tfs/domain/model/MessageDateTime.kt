package com.spinoza.messenger_tfs.domain.model

import com.spinoza.messenger_tfs.domain.util.EMPTY_STRING

data class MessageDateTime(
    val dateString: String = EMPTY_STRING,
    val timeString: String = EMPTY_STRING,
    val dateTimestamp: Long = EMPTY_TIMESTAMP,
    val fullTimeStamp: Long = EMPTY_TIMESTAMP,
) : Comparable<MessageDateTime> {

    override fun compareTo(other: MessageDateTime): Int {
        return dateTimestamp.compareTo(other.dateTimestamp)
    }

    companion object {

        const val EMPTY_TIMESTAMP = 0L
    }
}