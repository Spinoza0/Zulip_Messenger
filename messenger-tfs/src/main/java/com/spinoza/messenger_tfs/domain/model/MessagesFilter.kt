package com.spinoza.messenger_tfs.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class MessagesFilter(
    val channel: Channel,
    val topic: Topic,
) : Parcelable