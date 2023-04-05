package com.spinoza.messenger_tfs.data.network.model.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteMessageEventDto(
    @SerialName("id") val id: Long,
    @SerialName("stream_id") val streamId: Long,
    @SerialName("message_id") val messageId: Long,
    @SerialName("message_type") val messageType: String,
    @SerialName("topic") val topic: String,
)