package com.spinoza.messenger_tfs.data.model.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateReactionResponseDto(
    @SerialName("result") val result: String,
    @SerialName("msg") val msg: String,
)