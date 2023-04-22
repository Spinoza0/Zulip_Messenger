package com.spinoza.messenger_tfs.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @param name String. The name of the topic.
 *
 * */
@Parcelize
data class Topic(
    val name: String = UNDEFINED_NAME,
    val messageCount: Int = NO_MESSAGES,
    val channelId: Long = Channel.UNDEFINED_ID,
) : Parcelable {

    companion object {

        const val UNDEFINED_NAME = ""
        const val NO_MESSAGES = 0
    }
}