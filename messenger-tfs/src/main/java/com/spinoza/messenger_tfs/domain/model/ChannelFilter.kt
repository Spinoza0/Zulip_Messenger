package com.spinoza.messenger_tfs.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ChannelFilter(
    val channel: Channel,
    val topicName: String,
) : Parcelable