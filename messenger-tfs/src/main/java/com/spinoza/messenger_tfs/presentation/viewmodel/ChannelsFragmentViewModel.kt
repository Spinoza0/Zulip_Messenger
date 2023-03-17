package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.spinoza.messenger_tfs.databinding.ChannelItemBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.repository.RepositoryState
import com.spinoza.messenger_tfs.domain.usecase.GetAllChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetSubscribedChannelsUseCase
import com.spinoza.messenger_tfs.presentation.model.ChannelsFragmentState
import com.spinoza.messenger_tfs.presentation.model.ChannelsFragmentState.SourceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChannelsFragmentViewModel(
    private val getAllChannelsUseCase: GetAllChannelsUseCase,
    private val getSubscribedChannelsUseCase: GetSubscribedChannelsUseCase,
) : ViewModel() {

    val state: StateFlow<ChannelsFragmentState>
        get() = _state.asStateFlow()

    private var source = SourceType.SUBSCRIBED

    private val channelsLocalCache = mutableListOf<Channel>()

    private val _state =
        MutableStateFlow<ChannelsFragmentState>(
            ChannelsFragmentState.Source(SourceType.SUBSCRIBED)
        )

    init {
        getChannels()
    }

    fun switchSource(newSource: SourceType) {
        val sourceChanged = newSource != source
        if (sourceChanged) {
            source = newSource
            getChannels()
            _state.value = ChannelsFragmentState.Source(source)
        }
    }

    fun onChannelClickListener(channel: Channel, itemBinding: ChannelItemBinding) {
        for (index in 0 until channelsLocalCache.size) {
            val oldChannel = channelsLocalCache[index]
            if (oldChannel.channelId == channel.channelId) {
                val newType = getNewChannelType(oldChannel.type)
                val newChannel = channel.copy(type = newType)
                channelsLocalCache[index] = newChannel
                val topics = if (newType == Channel.Type.FOLDED)
                    listOf()
                else
                    newChannel.topics
                _state.value = ChannelsFragmentState.Topics(topics, newChannel, itemBinding)
                break
            }
        }
    }

    private fun getNewChannelType(type: Channel.Type): Channel.Type {
        return if (type == Channel.Type.FOLDED) {
            Channel.Type.UNFOLDED
        } else {
            Channel.Type.FOLDED
        }
    }

    private fun getChannels() {
        when (source) {
            SourceType.SUBSCRIBED -> {
                _state.value = updateCache(getAllChannelsUseCase())
            }
            SourceType.ALL -> {
                _state.value = updateCache(getSubscribedChannelsUseCase())
            }
        }
    }

    private fun updateCache(state: RepositoryState): ChannelsFragmentState {
        return when (state) {
            is RepositoryState.Channels -> {
                channelsLocalCache.clear()
                channelsLocalCache.addAll(state.channels)
                ChannelsFragmentState.Channels(channelsLocalCache)
            }
            is RepositoryState.Error -> ChannelsFragmentState.Error(state.text)
            else -> ChannelsFragmentState.Error(UNKNOWN_ERROR)
        }
    }

    companion object {
        private const val UNKNOWN_ERROR = ""
    }
}