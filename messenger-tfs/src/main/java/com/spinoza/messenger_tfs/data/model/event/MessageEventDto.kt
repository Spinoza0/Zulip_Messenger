package com.spinoza.messenger_tfs.data.model.event

import com.spinoza.messenger_tfs.data.model.message.MessageDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageEventDto(
    @SerialName("id") val id: Long,
    @SerialName("message") val message: MessageDto,
    @SerialName("flags") val flags: List<String>,
)