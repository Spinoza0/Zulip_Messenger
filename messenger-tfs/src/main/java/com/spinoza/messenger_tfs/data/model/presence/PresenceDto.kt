package com.spinoza.messenger_tfs.data.model.presence

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresenceDto(
    @SerialName("aggregated") val aggregated: PresenceDataDto,
)