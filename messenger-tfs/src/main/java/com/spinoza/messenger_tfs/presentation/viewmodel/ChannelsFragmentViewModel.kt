package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.usecase.GetAllChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetSubscribedChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetTopicsUseCase
import com.spinoza.messenger_tfs.presentation.adapter.channels.ChannelDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.channels.TopicDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.model.ChannelItem
import com.spinoza.messenger_tfs.presentation.state.ChannelsScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChannelsFragmentViewModel(
    private val isAllChannels: Boolean,
    private val getTopicsUseCase: GetTopicsUseCase,
    private val getSubscribedChannelsUseCase: GetSubscribedChannelsUseCase,
    private val getAllChannelsUseCase: GetAllChannelsUseCase,
) : ViewModel() {

    val state: StateFlow<ChannelsScreenState>
        get() = _state.asStateFlow()
    private val _state =
        MutableStateFlow<ChannelsScreenState>(ChannelsScreenState.Loading)

    private val cache = mutableListOf<DelegateAdapterItem>()

    fun loadItems(channelsFilter: ChannelsFilter) {
        viewModelScope.launch {
            when (val result = if (isAllChannels)
                getAllChannelsUseCase(channelsFilter)
            else
                getSubscribedChannelsUseCase(channelsFilter)
            ) {
                is RepositoryResult.Success -> {
                    updateCache(result.value.toDelegateItem(isAllChannels))
                    _state.value = ChannelsScreenState.Items(cache.toList())
                }
                // TODO: process other errors
                is RepositoryResult.Failure -> {}
            }
        }
    }

    fun onChannelClickListener(channelItem: ChannelItem) {
        viewModelScope.launch {
            val oldChannelDelegateItem = cache.find { delegateAdapterItem ->
                if (delegateAdapterItem is ChannelDelegateItem) {
                    val item = delegateAdapterItem.content() as ChannelItem
                    item.channel.channelId == channelItem.channel.channelId
                } else false
            }

            if (oldChannelDelegateItem != null) {
                val index = cache.indexOf(oldChannelDelegateItem)
                val oldChannelItem = oldChannelDelegateItem.content() as ChannelItem
                val newChannelDelegateItem = ChannelDelegateItem(
                    oldChannelItem.copy(isFolded = !oldChannelItem.isFolded)
                )
                cache[index] = newChannelDelegateItem

                if (oldChannelItem.isFolded) {
                    val topicsResult =
                        getTopicsUseCase(channelItem.channel.channelId)
                    if (topicsResult is RepositoryResult.Success) {
                        cache.addAll(
                            index + 1,
                            topicsResult.value.toDelegateItem(channelItem.channel)
                        )
                    }
                    // TODO: process other errors
                } else {
                    var nextIndex = index + 1
                    var isNextElementExist = false
                    while (nextIndex < cache.size && !isNextElementExist) {
                        if (cache[nextIndex] is ChannelDelegateItem) {
                            isNextElementExist = true
                        } else {
                            nextIndex++
                        }
                    }
                    if (isNextElementExist) {
                        cache.subList(index + 1, nextIndex).clear()
                    } else {
                        cache.subList(index + 1, cache.size).clear()
                    }
                }
                _state.value = ChannelsScreenState.Items(cache.toList())
            }
        }
    }

    private fun updateCache(newChannels: List<DelegateAdapterItem>) {
        cache.clear()
        cache.addAll(newChannels)
    }

    private fun Channel.toDelegateItem(isAllChannels: Boolean): ChannelDelegateItem {
        return ChannelDelegateItem(ChannelItem(this, isAllChannels, true))
    }

    private fun List<Channel>.toDelegateItem(isAllChannels: Boolean): List<ChannelDelegateItem> {
        return map { it.toDelegateItem(isAllChannels) }
    }

    private fun Topic.toDelegateItem(channel: Channel): TopicDelegateItem {
        return TopicDelegateItem(MessagesFilter(channel, this))
    }

    private fun List<Topic>.toDelegateItem(channel: Channel): List<TopicDelegateItem> {
        return map { it.toDelegateItem(channel) }
    }

    override fun onCleared() {
        super.onCleared()

        cache.clear()
    }
}