package com.spinoza.messenger_tfs.domain.model

data class User(
    val userId: Long,
    val email: String,
    val full_name: String,
    val avatar_url: String,
    val presence: Presence,
) {
    enum class Presence {
        ONLINE,
        IDLE,
        OFFLINE
    }
}