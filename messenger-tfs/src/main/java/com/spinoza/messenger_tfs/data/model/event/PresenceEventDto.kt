package com.spinoza.messenger_tfs.data.model.event

import com.spinoza.messenger_tfs.data.model.presence.PresenceDataDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresenceEventDto(
    @SerialName("type") val type: String,
    @SerialName("user_id") val userId: Int,
    @SerialName("email") val email: String,
    @SerialName("presence") val presence: Map<String, PresenceDataDto>,
    @SerialName("id") val id: Int,
)