package com.spinoza.messenger_tfs.data.network.model.message

import com.spinoza.messenger_tfs.data.network.ZulipResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SingleMessageResponse(
    @SerialName("result") override val result: String,
    @SerialName("msg") override val msg: String,
    @SerialName("message") val message: MessageDto,
) : ZulipResponse