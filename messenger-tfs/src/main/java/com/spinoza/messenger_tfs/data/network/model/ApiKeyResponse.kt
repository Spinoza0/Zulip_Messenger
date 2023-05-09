package com.spinoza.messenger_tfs.data.network.model

import com.spinoza.messenger_tfs.data.network.apiservice.ZulipResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiKeyResponse(
    @SerialName("result") override val result: String,
    @SerialName("msg") override val msg: String,
    @SerialName("api_key") val apiKey: String,
    @SerialName("email") val email: String,
) : ZulipResponse