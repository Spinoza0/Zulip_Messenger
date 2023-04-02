package com.spinoza.messenger_tfs.data.model.event

import com.spinoza.messenger_tfs.data.model.presence.PresenceDataDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresenceEventDto(
    @SerialName("id") val id: Long,
    @SerialName("type") val type: String,
    @SerialName("user_id") val userId: Long,
    @SerialName("email") val email: String,
    @SerialName("presence") val presence: Map<String, PresenceDataDto>,
)