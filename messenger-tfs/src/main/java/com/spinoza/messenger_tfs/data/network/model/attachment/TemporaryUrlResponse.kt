package com.spinoza.messenger_tfs.data.network.model.attachment

import com.spinoza.messenger_tfs.data.network.apiservice.ZulipResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TemporaryUrlResponse(
    @SerialName("result") override val result: String,
    @SerialName("msg") override val msg: String,
    @SerialName("url") val url: String,
) : ZulipResponse