package com.spinoza.messenger_tfs.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class MessageEntity(
    val name: String,
    val text: String,
    val reactions: List<ReactionEntity>,
    val iconAddVisibility: Boolean,

    // TODO: work with userId, date
    val userId: String = "",
    val date: String = "08.03.2023"
) : Parcelable