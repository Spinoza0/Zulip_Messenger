package com.spinoza.messenger_tfs.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReactionEntity(
    val emoji: String,
    val count: Int,
    val selected: Boolean,
) : Parcelable