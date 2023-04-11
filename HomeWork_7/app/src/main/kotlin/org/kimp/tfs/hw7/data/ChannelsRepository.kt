package org.kimp.tfs.hw7.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.kimp.tfs.hw7.data.api.Stream
import org.kimp.tfs.hw7.data.api.Topic

class ChannelsRepository(
    private val zulipService: ZulipService
) {
    fun getSubscribedChannels(): Flow<Map<Stream, List<Topic>>> = flow {
        val subscribedStreams = zulipService.getSubscribedStreams().subscriptions
        emit(createChannelsMap(subscribedStreams))
    }

    fun getAllChannels(): Flow<Map<Stream, List<Topic>>> = flow {
        val streams = zulipService.getAllStreams().streams
        emit(createChannelsMap(streams))
    }

    private suspend fun createChannelsMap(streams: List<Stream>) = streams.asSequence()
        .associateWith { s -> zulipService.getTopicsInStream(s.id).topics }
}
