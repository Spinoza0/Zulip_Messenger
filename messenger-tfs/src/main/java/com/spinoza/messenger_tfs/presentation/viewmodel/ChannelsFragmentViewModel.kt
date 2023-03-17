package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.databinding.ChannelItemBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.repository.RepositoryState
import com.spinoza.messenger_tfs.domain.usecase.GetChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetTopicsUseCase
import com.spinoza.messenger_tfs.presentation.model.ChannelsFragmentState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChannelsFragmentViewModel(
    private val getChannelsUseCase: GetChannelsUseCase,
    private val getTopicsUseCase: GetTopicsUseCase,
) : ViewModel() {

    val state: StateFlow<ChannelsFragmentState>
        get() = _state.asStateFlow()

    private val channelsLocalCache = mutableListOf<Channel>()

    private val _state =
        MutableStateFlow<ChannelsFragmentState>(ChannelsFragmentState.Idle)

    init {
        getChannels()
    }

    fun onChannelClickListener(channel: Channel, itemBinding: ChannelItemBinding) {
        viewModelScope.launch {
            changeChannelType(channel, itemBinding)
        }
    }

    private suspend fun changeChannelType(channel: Channel, itemBinding: ChannelItemBinding) {
        for (index in 0 until channelsLocalCache.size) {
            val oldChannel = channelsLocalCache[index]
            if (oldChannel.channelId == channel.channelId) {
                val newType = getNewChannelType(oldChannel.type)
                val newChannel = channel.copy(type = newType)
                var success = true
                if (newType == Channel.Type.FOLDED) {
                    _state.value = ChannelsFragmentState.Topics(listOf(), newChannel, itemBinding)
                } else {
                    val result = getTopicsUseCase(channel.channelId)
                    if (result is RepositoryState.Topics) {
                        _state.value =
                            ChannelsFragmentState.Topics(result.topics, newChannel, itemBinding)
                    } else if (result is RepositoryState.Error) {
                        _state.value = ChannelsFragmentState.Error(result.text)
                        success = false
                    }
                }
                if (success) {
                    channelsLocalCache[index] = newChannel
                }
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
        viewModelScope.launch {
            _state.value = updateCache(getChannelsUseCase())
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