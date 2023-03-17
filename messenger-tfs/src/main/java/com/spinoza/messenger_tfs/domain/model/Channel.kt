package com.spinoza.messenger_tfs.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Channel(
    val channelId: Long,
    val name: String,
    val type: Type = Type.FOLDED,
) : Parcelable {

    enum class Type { FOLDED, UNFOLDED }
}