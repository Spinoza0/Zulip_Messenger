package com.spinoza.messenger_tfs.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @param name String. The name of the topic.
 *
 * */
@Parcelize
data class Topic(
    val name: String,
    val messageCount: Int,
) : Parcelable