package com.spinoza.messenger_tfs.data.network.model.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class RegisterEventQueueResponse(
    @SerialName("result") val result: String,
    @SerialName("msg") val msg: String,
    @SerialName("queue_id") val queueId: String,
    @SerialName("last_event_id") val lastEventId: Long,
)