package com.spinoza.messenger_tfs.data.network.model.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HeartBeatEventDto(
    @SerialName("id") val id: Long,
    @SerialName("type") val type: String,
)