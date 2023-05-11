package com.spinoza.messenger_tfs.data.network.model.stream

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionItemDto(
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
)