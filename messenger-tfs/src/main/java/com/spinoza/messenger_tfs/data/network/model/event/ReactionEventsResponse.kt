package com.spinoza.messenger_tfs.data.network.model.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ReactionEventsResponse(
    @SerialName("result") val result: String,
    @SerialName("msg") val msg: String,
    @SerialName("events") val events: List<ReactionEventDto>,
)