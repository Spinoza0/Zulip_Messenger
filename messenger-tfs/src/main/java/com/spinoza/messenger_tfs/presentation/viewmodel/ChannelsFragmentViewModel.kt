package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.databinding.ChannelItemBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.repository.RepositoryState
import com.spinoza.messenger_tfs.domain.usecase.GetAllChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetSubscribedChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetTopicsUseCase
import com.spinoza.messenger_tfs.presentation.model.ChannelsFragmentState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChannelsFragmentViewModel(
    private val getTopicsUseCase: GetTopicsUseCase,
    private val getSubscribedChannelsUseCase: GetSubscribedChannelsUseCase,
    private val getAllChannelsUseCase: GetAllChannelsUseCase,
) : ViewModel() {

    val stateAllChannels: StateFlow<ChannelsFragmentState>
        get() = _stateAllChannels.asStateFlow()
    val stateSubscribedChannels: StateFlow<ChannelsFragmentState>
        get() = _stateSubscribedChannels.asStateFlow()

    private val allChannelsCache = mutableListOf<Channel>()
    private val subscribedChannelsCache = mutableListOf<Channel>()
    private val _stateAllChannels =
        MutableStateFlow<ChannelsFragmentState>(ChannelsFragmentState.Idle)
    private val _stateSubscribedChannels =
        MutableStateFlow<ChannelsFragmentState>(ChannelsFragmentState.Idle)

    fun getChannels(allChannels: Boolean) {
        viewModelScope.launch {
            val cache: MutableList<Channel>
            val state: RepositoryState
            if (allChannels) {
                cache = allChannelsCache
                state = getAllChannelsUseCase()
            } else {
                cache = subscribedChannelsCache
                state = getSubscribedChannelsUseCase()
            }

            val result = when (state) {
                is RepositoryState.Channels -> {
                    cache.clear()
                    cache.addAll(state.channels)
                    ChannelsFragmentState.Channels(cache)
                }
                is RepositoryState.Error -> ChannelsFragmentState.Error(state.text)
                else -> ChannelsFragmentState.Error(UNKNOWN_ERROR)
            }

            if (allChannels) {
                _stateAllChannels.value = result
            } else {
                _stateSubscribedChannels.value = result
            }
        }
    }

    fun onChannelClickListener(
        allChannels: Boolean,
        channel: Channel,
        itemBinding: ChannelItemBinding,
    ) {
        viewModelScope.launch {
            val cache = if (allChannels) {
                allChannelsCache
            } else {
                subscribedChannelsCache
            }

            for (index in 0 until cache.size) {
                val oldChannel = cache[index]
                if (oldChannel.channelId == channel.channelId) {
                    val newType = getNewChannelType(oldChannel.type)
                    val newChannel = channel.copy(type = newType)
                    var success = true
                    var result: ChannelsFragmentState = ChannelsFragmentState.Idle
                    if (newType == Channel.Type.FOLDED) {
                        result = ChannelsFragmentState.Topics(listOf(), newChannel, itemBinding)
                    } else {
                        val repositoryState = getTopicsUseCase(channel.channelId)
                        if (repositoryState is RepositoryState.Topics) {
                            result =
                                ChannelsFragmentState.Topics(
                                    repositoryState.topics,
                                    newChannel,
                                    itemBinding
                                )
                        } else if (repositoryState is RepositoryState.Error) {
                            result = ChannelsFragmentState.Error(repositoryState.text)
                            success = false
                        }
                    }
                    if (allChannels) {
                        _stateAllChannels.value = result
                    } else {
                        _stateSubscribedChannels.value = result
                    }

                    if (success) {
                        cache[index] = newChannel
                    }
                    break
                }
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

    companion object {
        // TODO: process errors
        private const val UNKNOWN_ERROR = ""
    }
}