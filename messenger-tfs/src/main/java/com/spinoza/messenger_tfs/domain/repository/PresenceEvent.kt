package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.User

data class PresenceEvent(
    val id: Long,
    val userId: Long,
    val email: String,
    val presence: User.Presence,
)