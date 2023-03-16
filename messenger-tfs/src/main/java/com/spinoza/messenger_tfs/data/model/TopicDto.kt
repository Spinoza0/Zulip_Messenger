package com.spinoza.messenger_tfs.data.model

/**
 * @param maxId Int. The message ID of the last message sent to this topic.
 * @param name String. The name of the topic.
 *
 * */
class TopicDto(
    val maxId: Long,
    val name: String,
)