package com.spinoza.messenger_tfs.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val userId: Long,
    val email: String,
    val fullName: String,
    val avatarUrl: String,
    val presence: Presence,
) : Parcelable {

    enum class Presence {
        ACTIVE,
        IDLE,
        OFFLINE
    }
}