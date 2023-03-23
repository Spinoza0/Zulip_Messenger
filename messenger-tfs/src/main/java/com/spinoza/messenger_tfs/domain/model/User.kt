package com.spinoza.messenger_tfs.domain.model

data class User(
    val userId: Long,
    val isActive: Boolean,
    val email: String,
    val full_name: String,
    val avatar_url: String,
    val status: String,
)