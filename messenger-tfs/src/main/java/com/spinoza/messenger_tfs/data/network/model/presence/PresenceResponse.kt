package com.spinoza.messenger_tfs.data.network.model.presence

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresenceResponse(
    @SerialName("result") val result: String,
    @SerialName("msg") val msg: String,
    @SerialName("presence") val presence: PresenceDto
)