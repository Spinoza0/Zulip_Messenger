package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelFilter
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
    private val getTopicsUseCase: GetTopicsUseCase,
    private val getSubscribedChannelsUseCase: GetSubscribedChannelsUseCase,
    private val getAllChannelsUseCase: GetAllChannelsUseCase,
) : ViewModel() {

    val stateAllItems: StateFlow<ChannelsScreenState>
        get() = _stateAllItems.asStateFlow()
    val stateSubscribedItems: StateFlow<ChannelsScreenState>
        get() = _stateSubscribedItems.asStateFlow()

    private val allItemsCache = mutableListOf<DelegateAdapterItem>()
    private val subscribedItemsCache = mutableListOf<DelegateAdapterItem>()
    private val _stateAllItems =
        MutableStateFlow<ChannelsScreenState>(ChannelsScreenState.Loading)
    private val _stateSubscribedItems =
        MutableStateFlow<ChannelsScreenState>(ChannelsScreenState.Loading)

    fun loadItems(isAllChannels: Boolean) {
        viewModelScope.launch {
            val cache: MutableList<DelegateAdapterItem>
            val state: MutableStateFlow<ChannelsScreenState>
            val channelsResult: Pair<RepositoryResult, List<Channel>>
            if (isAllChannels) {
                cache = allItemsCache
                state = _stateAllItems
                channelsResult = getAllChannelsUseCase()
            } else {
                cache = subscribedItemsCache
                state = _stateSubscribedItems
                channelsResult = getSubscribedChannelsUseCase()
            }

            if (channelsResult.first.type == RepositoryResult.Type.SUCCESS) {
                cache.clear()
                cache.addAll(channelsResult.second.toDelegateItem(isAllChannels))
                state.value = ChannelsScreenState.Items(cache.toList())
            } else {
                state.value = ChannelsScreenState.Error(channelsResult.first)
            }
        }
    }

    fun onChannelClickListener(channelItem: ChannelItem) {
        viewModelScope.launch {
            val state: MutableStateFlow<ChannelsScreenState>
            val cache: MutableList<DelegateAdapterItem>
            if (channelItem.isAllChannelsItem) {
                cache = allItemsCache
                state = _stateAllItems
            } else {
                cache = subscribedItemsCache
                state = _stateSubscribedItems
            }

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
                    if (topicsResult.first.type == RepositoryResult.Type.SUCCESS) {
                        cache.addAll(
                            index + 1,
                            topicsResult.second.toDelegateItem(channelItem.channel)
                        )
                    }
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
                state.value = ChannelsScreenState.Items(cache.toList())
            }
        }
    }

    private fun Channel.toDelegateItem(isAllChannels: Boolean): ChannelDelegateItem {
        return ChannelDelegateItem(ChannelItem(this, isAllChannels, true))
    }

    private fun List<Channel>.toDelegateItem(isAllChannels: Boolean): List<ChannelDelegateItem> {
        return map { it.toDelegateItem(isAllChannels) }
    }

    private fun Topic.toDelegateItem(channel: Channel): TopicDelegateItem {
        return TopicDelegateItem(ChannelFilter(channel, this))
    }

    private fun List<Topic>.toDelegateItem(channel: Channel): List<TopicDelegateItem> {
        return map { it.toDelegateItem(channel) }
    }

    override fun onCleared() {
        super.onCleared()

        allItemsCache.clear()
        subscribedItemsCache.clear()
    }
}