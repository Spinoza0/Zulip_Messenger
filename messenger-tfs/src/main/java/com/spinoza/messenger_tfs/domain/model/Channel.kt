package com.spinoza.messenger_tfs.domain.model

/**
 * @param streamId Int. The ID of the stream.
 * @param name String. The name of the Channel.
 * */
data class Channel(
    val streamId: Long,
    val name: String,
    val topics: List<Topic>,
)