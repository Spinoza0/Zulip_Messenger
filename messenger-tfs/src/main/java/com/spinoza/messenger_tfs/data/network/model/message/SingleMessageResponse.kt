package com.spinoza.messenger_tfs.data.network.model.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SingleMessageResponse(
    @SerialName("result") val result: String,
    @SerialName("msg") val msg: String,
    @SerialName("message") val message: MessageDto,
)