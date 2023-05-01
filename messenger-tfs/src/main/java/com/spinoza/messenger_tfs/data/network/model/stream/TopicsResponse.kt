package com.spinoza.messenger_tfs.data.network.model.stream

import com.spinoza.messenger_tfs.data.network.ZulipResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopicsResponse(
    @SerialName("result") override val result: String,
    @SerialName("msg") override val msg: String,
    @SerialName("topics") val topics: List<TopicDto>,
) : ZulipResponse