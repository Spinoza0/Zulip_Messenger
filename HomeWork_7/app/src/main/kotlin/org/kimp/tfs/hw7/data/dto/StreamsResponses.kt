package org.kimp.tfs.hw7.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.kimp.tfs.hw7.data.api.Stream

@Serializable
data class StreamsResponse(
    @SerialName("streams") val streams: List<Stream>
)

@Serializable
data class SubscribedStreamsResponse(
    @SerialName("subscriptions") val subscriptions: List<Stream>
)
