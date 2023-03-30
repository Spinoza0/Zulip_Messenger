package com.spinoza.messenger_tfs.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDataDto(
    @SerialName("value") val value: String = "",
    @SerialName("rendered_value") val rendered_value: String = "",
)