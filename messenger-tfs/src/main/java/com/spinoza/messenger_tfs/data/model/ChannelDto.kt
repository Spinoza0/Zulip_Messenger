package com.spinoza.messenger_tfs.data.model

/**
 * @param id Int. The ID of the stream.
 * @param name String. The name of the Channel.
 * */
data class ChannelDto(
    val id: Long,
    val name: String,
    val topics: List<TopicDto>,
)