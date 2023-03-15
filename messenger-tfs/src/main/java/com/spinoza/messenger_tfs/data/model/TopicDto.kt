package com.spinoza.messenger_tfs.data.model

/**
 * @param max_id Int. The message ID of the last message sent to this topic.
 * @param name String. The name of the topic.
 *
 * */
class TopicDto(
    val max_id: Int,
    val name: String,
)