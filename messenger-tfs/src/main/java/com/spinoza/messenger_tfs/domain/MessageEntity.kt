package com.spinoza.messenger_tfs.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class MessageEntity(
    val name: String,
    val text: String,
    val reactions: List<ReactionEntity>,
    val iconAddVisibility: Boolean,
) : Parcelable