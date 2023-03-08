package com.spinoza.messenger_tfs.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReactionEntity(
    val emoji: String,
    val count: Int,
    val isSelected: Boolean,

    // TODO: work with userId
    val userId: String = ""
) : Parcelable