package com.spinoza.messenger_tfs.data.model.stream

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscribedStreamsResponse(
    @SerialName("result") val result: String,
    @SerialName("msg") val msg: String,
    @SerialName("subscriptions") val subscriptions: List<StreamDto>,
)