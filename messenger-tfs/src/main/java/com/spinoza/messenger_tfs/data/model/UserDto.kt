package com.spinoza.messenger_tfs.data.model

import com.spinoza.messenger_tfs.domain.model.User

data class UserDto(
    val userId: Long,
    val email: String,
    val full_name: String,
    val avatar_url: String?,
    val presence: User.Presence,
)