package com.spinoza.messenger_tfs.data.network.model.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NarrowOperatorItemDto(
    @SerialName("operator") val operator: String,
    @SerialName("operand") val operand: String,
)