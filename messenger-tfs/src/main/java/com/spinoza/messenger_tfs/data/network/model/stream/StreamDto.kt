package com.spinoza.messenger_tfs.data.network.model.stream

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreamDto(
    @SerialName("date_created") val dateCreated: Long,
    @SerialName("first_message_id") val firstMessageId: Long?,
    @SerialName("invite_only") val inviteOnly: Boolean,
    @SerialName("is_announcement_only") val isAnnouncementOnly: Boolean,
    @SerialName("name") val name: String,
    @SerialName("stream_id") val streamId: Long,
)