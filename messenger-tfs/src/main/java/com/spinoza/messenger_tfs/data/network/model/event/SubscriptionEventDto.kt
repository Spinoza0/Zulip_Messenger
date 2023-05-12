package com.spinoza.messenger_tfs.data.network.model.event

import com.spinoza.messenger_tfs.data.network.model.stream.StreamDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionEventDto(
    @SerialName("id") val id: Long,
    @SerialName("type") val type: String,
    @SerialName("op") val operation: String,
    @SerialName("subscriptions") val streams: List<StreamDto>,
)