package com.spinoza.messenger_tfs.data.model

/**
 * @param name String. The name of the topic.
 * @param maxId Long. The message ID of the last message sent to this topic.
 * */
data class TopicDto(
    val name: String,
    val maxId: Long,
)