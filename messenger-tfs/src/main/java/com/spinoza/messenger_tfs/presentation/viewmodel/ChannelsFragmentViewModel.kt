package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.databinding.ChannelItemBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
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
        MutableStateFlow<ChannelsFragmentState>(ChannelsFragmentState.Loading)
    private val _stateSubscribedChannels =
        MutableStateFlow<ChannelsFragmentState>(ChannelsFragmentState.Loading)

    fun getChannels(allChannels: Boolean) {
        viewModelScope.launch {
            val cache: MutableList<Channel>
            val state: MutableStateFlow<ChannelsFragmentState>
            val repositoryResult: Pair<RepositoryResult, List<Channel>>
            if (allChannels) {
                cache = allChannelsCache
                state = _stateAllChannels
                repositoryResult = getAllChannelsUseCase()
            } else {
                cache = subscribedChannelsCache
                state = _stateSubscribedChannels
                repositoryResult = getSubscribedChannelsUseCase()
            }

            if (repositoryResult.first.type == RepositoryResult.Type.SUCCESS) {
                cache.clear()
                cache.addAll(repositoryResult.second)
                state.value = ChannelsFragmentState.Channels(cache)
            } else {
                state.value = ChannelsFragmentState.Error(repositoryResult.first)
            }
        }
    }

    fun onChannelClickListener(
        allChannels: Boolean,
        channel: Channel,
        itemBinding: ChannelItemBinding,
    ) {
        viewModelScope.launch {
            val state: MutableStateFlow<ChannelsFragmentState>
            val cache: MutableList<Channel>
            if (allChannels) {
                cache = allChannelsCache
                state = _stateAllChannels
            } else {
                cache = subscribedChannelsCache
                state = _stateSubscribedChannels
            }

            val oldChannel = cache.find { it.channelId == channel.channelId }
            if (oldChannel != null) {
                val index = cache.indexOf(oldChannel)
                val newType = getNewChannelType(oldChannel.type)
                val newChannel = channel.copy(type = newType)
                state.value = ChannelsFragmentState.Loading
                if (newType == Channel.Type.FOLDED) {
                    cache[index] = newChannel
                    state.value = ChannelsFragmentState.Topics(listOf(), newChannel, itemBinding)
                } else {
                    val repositoryState = getTopicsUseCase(channel.channelId)
                    if (repositoryState.first.type == RepositoryResult.Type.SUCCESS) {
                        cache[index] = newChannel
                        state.value = ChannelsFragmentState.Topics(
                            repositoryState.second,
                            newChannel,
                            itemBinding
                        )
                    } else {
                        state.value = ChannelsFragmentState.Error(repositoryState.first)
                    }
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
}