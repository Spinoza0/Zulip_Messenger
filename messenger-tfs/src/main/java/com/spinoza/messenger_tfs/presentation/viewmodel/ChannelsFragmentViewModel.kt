package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.databinding.ChannelItemBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.usecase.GetAllChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetSubscribedChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetTopicsUseCase
import com.spinoza.messenger_tfs.presentation.model.ChannelItem
import com.spinoza.messenger_tfs.presentation.model.ChannelsFragmentState
import com.spinoza.messenger_tfs.presentation.model.toChannelItem
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

    private val allChannelsCache = mutableListOf<ChannelItem>()
    private val subscribedChannelsCache = mutableListOf<ChannelItem>()
    private val _stateAllChannels =
        MutableStateFlow<ChannelsFragmentState>(ChannelsFragmentState.Loading)
    private val _stateSubscribedChannels =
        MutableStateFlow<ChannelsFragmentState>(ChannelsFragmentState.Loading)

    fun getChannels(isAllChannels: Boolean) {
        viewModelScope.launch {
            val cache: MutableList<ChannelItem>
            val state: MutableStateFlow<ChannelsFragmentState>
            val repositoryResult: Pair<RepositoryResult, List<Channel>>
            if (isAllChannels) {
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
                cache.addAll(repositoryResult.second.toChannelItem())
                state.value = ChannelsFragmentState.Channels(cache)
            } else {
                state.value = ChannelsFragmentState.Error(repositoryResult.first)
            }
        }
    }

    fun onChannelClickListener(
        isAllChannels: Boolean,
        channelItem: ChannelItem,
        itemBinding: ChannelItemBinding,
    ) {
        viewModelScope.launch {
            val state: MutableStateFlow<ChannelsFragmentState>
            val cache: MutableList<ChannelItem>
            if (isAllChannels) {
                cache = allChannelsCache
                state = _stateAllChannels
            } else {
                cache = subscribedChannelsCache
                state = _stateSubscribedChannels
            }

            val oldChannel = cache.find {
                it.channel.channelId == channelItem.channel.channelId
            }
            if (oldChannel != null) {
                val index = cache.indexOf(oldChannel)
                val newType = getNewChannelType(oldChannel.type)
                val newChannel = channelItem.copy(type = newType)
                state.value = ChannelsFragmentState.Loading
                if (newType == ChannelItem.Type.FOLDED) {
                    cache[index] = newChannel
                    state.value = ChannelsFragmentState.Topics(listOf(), newChannel, itemBinding)
                } else {
                    val repositoryState =
                        getTopicsUseCase(channelItem.channel.channelId)
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

    private fun getNewChannelType(type: ChannelItem.Type): ChannelItem.Type {
        return if (type == ChannelItem.Type.FOLDED) {
            ChannelItem.Type.UNFOLDED
        } else {
            ChannelItem.Type.FOLDED
        }
    }
}