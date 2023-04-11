package org.kimp.tfs.hw7.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Topic(
    @SerialName("max_id")
    val maxId: Int,
    @SerialName("name")
    val name: String,
)
