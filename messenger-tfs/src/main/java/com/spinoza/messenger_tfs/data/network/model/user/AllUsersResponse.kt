package com.spinoza.messenger_tfs.data.network.model.user

import com.spinoza.messenger_tfs.data.network.apiservice.ZulipResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AllUsersResponse(
    @SerialName("result") override val result: String,
    @SerialName("msg") override val msg: String,
    @SerialName("members") val members: List<UserDto>,
) : ZulipResponse