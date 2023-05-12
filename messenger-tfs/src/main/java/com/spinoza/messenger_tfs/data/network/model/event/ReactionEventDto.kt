package com.spinoza.messenger_tfs.data.network.model.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReactionEventDto(
    @SerialName("id") val id: Long,
    @SerialName("op") val operation: String,
    @SerialName("user_id") val userId: Long,
    @SerialName("message_id") val messageId: Long,
    @SerialName("emoji_name") val emoji_name: String,
    @SerialName("emoji_code") val emoji_code: String,
    @SerialName("reaction_type") val reaction_type: String,
)