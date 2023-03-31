package com.spinoza.messenger_tfs.domain.model

data class User(
    val userId: Long,
    val email: String,
    val fullName: String,
    val avatarUrl: String,
    val presence: Presence,
) {
    enum class Presence {
        ONLINE,
        IDLE,
        OFFLINE
    }
}