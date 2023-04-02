package com.spinoza.messenger_tfs.data.model.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class MessageEventsResponse(
    @SerialName("result") val result: String,
    @SerialName("msg") val msg: String,
    @SerialName("events") val events: List<MessageEventDto>,
)