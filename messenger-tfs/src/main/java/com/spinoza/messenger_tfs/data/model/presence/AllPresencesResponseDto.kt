package com.spinoza.messenger_tfs.data.model.presence

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AllPresencesResponseDto(
    @SerialName("result") val result: String,
    @SerialName("msg") val msg: String,
    @SerialName("presences") val presences: Map<String, PresenceDto>,
)