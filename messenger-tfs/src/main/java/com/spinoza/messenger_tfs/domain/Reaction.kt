package com.spinoza.messenger_tfs.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Reaction(
    val emoji: String,
    val count: Int,
    val selected: Boolean
) : Parcelable