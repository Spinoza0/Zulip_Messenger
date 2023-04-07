package com.spinoza.messenger_tfs.data.network.model.presence

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresenceDataDto(
    @SerialName("status") val status: String,
    @SerialName("timestamp") val timestamp: Long,
)