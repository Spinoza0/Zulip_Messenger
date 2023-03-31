package com.spinoza.messenger_tfs.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscribedStreamsResponseDto(
    @SerialName("result") val result: String,
    @SerialName("msg") val msg: String,
    @SerialName("subscriptions") val subscriptions: List<SubscribedStreamDto>,
)