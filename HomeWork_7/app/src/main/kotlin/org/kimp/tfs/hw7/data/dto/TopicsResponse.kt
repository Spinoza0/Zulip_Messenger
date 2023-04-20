package org.kimp.tfs.hw7.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.kimp.tfs.hw7.data.api.Topic

@Serializable
data class TopicsResponse(
    @SerialName("topics") val topics: List<Topic>
)
