package com.spinoza.messenger_tfs.data.model.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NarrowItemDto(
    @SerialName("operator") val operator: String,
    @SerialName("operand") val operand: String,
)