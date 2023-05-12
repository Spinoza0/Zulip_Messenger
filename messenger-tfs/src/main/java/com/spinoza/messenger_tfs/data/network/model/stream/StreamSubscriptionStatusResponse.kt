package com.spinoza.messenger_tfs.data.network.model.stream

import com.spinoza.messenger_tfs.data.network.apiservice.ZulipResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreamSubscriptionStatusResponse(
    @SerialName("msg") override val msg: String,
    @SerialName("result") override val result: String,
    @SerialName("is_subscribed") val isSubscribed: Boolean,
) : ZulipResponse