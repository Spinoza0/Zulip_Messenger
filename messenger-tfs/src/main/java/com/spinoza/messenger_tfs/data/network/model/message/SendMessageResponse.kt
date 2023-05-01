package com.spinoza.messenger_tfs.data.network.model.message

import com.spinoza.messenger_tfs.data.network.ZulipResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendMessageResponse(
    @SerialName("result") override val result: String,
    @SerialName("msg") override val msg: String,
    @SerialName("id") val messageId: Long,
) : ZulipResponse