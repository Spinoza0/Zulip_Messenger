package com.spinoza.messenger_tfs.data.model

import com.spinoza.messenger_tfs.domain.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresenceDto(
    @SerialName("aggregated") val aggregated: PresenceDataDto,
) {
    fun toDomain(): User.Presence =
        when (aggregated.status) {
            "active" -> User.Presence.ONLINE
            "idle" -> User.Presence.IDLE
            else -> User.Presence.OFFLINE
        }
}
