package com.spinoza.messenger_tfs.data.network.model.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    @SerialName("id") val id: Long,
    @SerialName("stream_id") val streamId: Long,
    @SerialName("sender_id") val senderId: Long,
    @SerialName("content") val content: String,
    @SerialName("content_type") val content_type: String,
    @SerialName("recipient_id") val recipientId: Int,
    @SerialName("timestamp") val timestamp: Long,
    @SerialName("subject") val subject: String,
    @SerialName("is_me_message") val isMeMessage: Boolean,
    @SerialName("reactions") val reactions: List<ReactionDto>,
    @SerialName("sender_full_name") val senderFullName: String,
    @SerialName("sender_email") val senderEmail: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
) : Comparable<MessageDto> {

    override fun compareTo(other: MessageDto): Int {
        return id.compareTo(other.id)
    }
}