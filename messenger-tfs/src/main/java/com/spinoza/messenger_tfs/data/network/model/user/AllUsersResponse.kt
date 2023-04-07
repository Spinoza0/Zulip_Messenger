package com.spinoza.messenger_tfs.data.network.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AllUsersResponse(
    @SerialName("result") val result: String,
    @SerialName("msg") val msg: String,
    @SerialName("members") val members: List<UserDto>,
)