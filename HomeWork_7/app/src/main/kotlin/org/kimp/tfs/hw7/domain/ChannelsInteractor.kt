package org.kimp.tfs.hw7.domain

import kotlinx.coroutines.flow.Flow
import org.kimp.tfs.hw7.data.ChannelsRepository
import org.kimp.tfs.hw7.data.api.Stream
import org.kimp.tfs.hw7.data.api.Topic

interface ChannelsInteractor {
    fun getChannels(subscribedOnly: Boolean): Flow<Map<Stream, List<Topic>>>
}

class ChannelsInteractorImpl(
    private val channelsRepository: ChannelsRepository
) : ChannelsInteractor {
    override fun getChannels(subscribedOnly: Boolean) = when (subscribedOnly) {
        true -> channelsRepository.getSubscribedChannels()
        else -> channelsRepository.getAllChannels()
    }
}
