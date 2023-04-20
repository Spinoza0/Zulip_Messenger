package org.kimp.tfs.hw7.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDataEntry(
    val value: String,
    @SerialName("render_value") val renderValue: String,
)
