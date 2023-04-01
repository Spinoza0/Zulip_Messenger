package com.spinoza.messenger_tfs.data.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    @SerialName("result") val result: String,
    @SerialName("msg") val msg: String,
    @SerialName("user") val user: UserDto,
)