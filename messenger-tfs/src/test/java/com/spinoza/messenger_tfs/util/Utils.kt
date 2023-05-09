package com.spinoza.messenger_tfs.util

import com.spinoza.messenger_tfs.data.network.model.stream.StreamDto
import com.spinoza.messenger_tfs.data.utils.dtoToDomain
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter

fun createChannels(channelsFilter: ChannelsFilter): Result<List<Channel>> {
    var id = 0L
    val streams = mutableListOf<StreamDto>()
    repeat(5) {
        streams.add(createStream(id++))
    }
    return Result.success(streams.dtoToDomain(channelsFilter))
}

private fun createStream(streamId: Long): StreamDto {
    return StreamDto(
        streamId = streamId,
        name = "Name $streamId",
        dateCreated = 0L,
        firstMessageId = null,
        inviteOnly = false,
        isAnnouncementOnly = false
    )
}