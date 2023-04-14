package com.spinoza.messenger_tfs.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiKeyResponse(
    @SerialName("result") val result: String,
    @SerialName("msg") val msg: String,
    @SerialName("api_key") val apiKey: String,
    @SerialName("email") val email: String,
)