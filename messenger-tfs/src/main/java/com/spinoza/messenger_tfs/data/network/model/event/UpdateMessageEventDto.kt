package com.spinoza.messenger_tfs.data.network.model.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateMessageEventDto(
    @SerialName("id") val id: Long,
    @SerialName("stream_id") val streamId: Long,
    @SerialName("message_id") val messageId: Long,
    @SerialName("rendered_content") val renderedContent: String? = null,
    @SerialName("subject") val subject: String? = null,
)