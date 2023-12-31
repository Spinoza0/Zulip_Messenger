package com.spinoza.messenger_tfs.data.network.model.event

import com.spinoza.messenger_tfs.data.network.apiservice.ZulipResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class RegisterEventQueueResponse(
    @SerialName("result") override val result: String,
    @SerialName("msg") override val msg: String,
    @SerialName("queue_id") val queueId: String,
    @SerialName("last_event_id") val lastEventId: Long,
) : ZulipResponse