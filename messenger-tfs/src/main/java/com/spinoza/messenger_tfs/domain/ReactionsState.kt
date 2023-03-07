package com.spinoza.messenger_tfs.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ReactionsState(
    val value: List<Reaction>,
    val iconAddVisibility: Boolean,
) : Parcelable