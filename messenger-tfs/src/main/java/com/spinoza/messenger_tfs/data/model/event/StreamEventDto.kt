package com.spinoza.messenger_tfs.data.model.event

import com.spinoza.messenger_tfs.data.model.stream.StreamDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreamEventDto(
    @SerialName("id") val id: Long,
    @SerialName("op") val operation: String,
    @SerialName("streams") val streams: List<StreamDto>,
)