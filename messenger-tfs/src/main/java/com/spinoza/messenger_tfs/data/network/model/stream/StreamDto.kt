package com.spinoza.messenger_tfs.data.network.model.stream

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreamDto(
    @SerialName("name") val name: String,
    @SerialName("stream_id") val streamId: Long,
)