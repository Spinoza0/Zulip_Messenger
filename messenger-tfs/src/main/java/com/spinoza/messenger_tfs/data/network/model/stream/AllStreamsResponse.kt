package com.spinoza.messenger_tfs.data.network.model.stream

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AllStreamsResponse(
    @SerialName("result") val result: String,
    @SerialName("msg") val msg: String,
    @SerialName("streams") val streams: List<StreamDto>,
)