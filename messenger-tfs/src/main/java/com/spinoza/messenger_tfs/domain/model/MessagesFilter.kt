package com.spinoza.messenger_tfs.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MessagesFilter(
    val channel: Channel = Channel(),
    val topic: Topic = Topic(),
) : Parcelable