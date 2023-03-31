package com.spinoza.messenger_tfs.data.model.stream

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param name String. The name of the topic.
 * @param maxId Long. The message ID of the last message sent to this topic.
 * */
@Serializable
data class TopicDto(
    @SerialName("name") val name: String,
    @SerialName("max_id") val maxId: Long,
)