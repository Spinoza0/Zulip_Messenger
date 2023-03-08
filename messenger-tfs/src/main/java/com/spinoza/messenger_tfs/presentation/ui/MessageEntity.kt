package com.spinoza.messenger_tfs.presentation.ui

import android.os.Parcelable
import com.spinoza.messenger_tfs.domain.ReactionEntity
import kotlinx.parcelize.Parcelize

@Parcelize
class MessageEntity(
    val name: String,
    val text: String,
    val reactions: List<ReactionEntity>,
    val iconAddVisibility: Boolean,
) : Parcelable