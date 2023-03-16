package com.spinoza.messenger_tfs.data.model

import com.spinoza.messenger_tfs.domain.model.Topic

/**
 * @param id Int. The ID of the stream.
 * @param name String. The name of the Channel.
 * */
data class StreamDto(
    val id: Long,
    val name: String,
    val topics: List<Topic>,
)