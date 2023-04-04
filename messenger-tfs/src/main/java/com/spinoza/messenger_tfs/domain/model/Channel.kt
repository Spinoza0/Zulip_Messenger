package com.spinoza.messenger_tfs.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Channel(
    val channelId: Long = UNDEFINED_ID,
    val name: String = NO_NAME,
) : Parcelable {

    companion object {

        const val UNDEFINED_ID = -1L
        const val NO_NAME = ""
    }
}