package com.spinoza.messenger_tfs.domain.model

import android.os.Parcelable
import com.spinoza.messenger_tfs.domain.util.EMPTY_STRING
import kotlinx.parcelize.Parcelize

/**
 * @param name String. The name of the topic.
 *
 * */
@Parcelize
data class Topic(
    val name: String = EMPTY_STRING,
    val messageCount: Int = NO_MESSAGES,
    val channelId: Long = Channel.UNDEFINED_ID,
    val lastMessageId: Long = Message.UNDEFINED_ID,
) : Parcelable {

    companion object {

        const val NO_MESSAGES = 0
    }
}