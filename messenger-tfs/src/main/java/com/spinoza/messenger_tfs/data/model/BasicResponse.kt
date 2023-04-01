package com.spinoza.messenger_tfs.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BasicResponse(
    @SerialName("result") val result: String,
    @SerialName("msg") val msg: String,
)