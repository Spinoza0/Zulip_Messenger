package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.App
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.usecase.GetAllChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetSubscribedChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetTopicUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetTopicsUseCase
import com.spinoza.messenger_tfs.presentation.adapter.channels.ChannelDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.channels.TopicDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.model.ChannelItem
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import com.spinoza.messenger_tfs.presentation.state.ChannelsScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChannelsPageFragmentViewModel(
    private val isAllChannels: Boolean,
    private val getTopicsUseCase: GetTopicsUseCase,
    private val getSubscribedChannelsUseCase: GetSubscribedChannelsUseCase,
    private val getAllChannelsUseCase: GetAllChannelsUseCase,
    private val getTopicUseCase: GetTopicUseCase,
) : ViewModel() {

    val state: StateFlow<ChannelsScreenState>
        get() = _state.asStateFlow()
    private val _state =
        MutableStateFlow<ChannelsScreenState>(ChannelsScreenState.Loading)

    private val cache = mutableListOf<DelegateAdapterItem>()
    private val globalRouter = App.router
    private var isReturnFromMessagesScreen = false

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

    fun updateMessagesCount() {
        if (isReturnFromMessagesScreen) {
            viewModelScope.launch {
                for (i in 0 until cache.size) {
                    if (cache[i] is TopicDelegateItem) {
                        val messagesFilter = cache[i].content() as MessagesFilter
                        val result = getTopicUseCase(messagesFilter)
                        if (result is RepositoryResult.Success) {
                            cache[i] = TopicDelegateItem(messagesFilter.copy(topic = result.value))
                        }
                    }
                }
                _state.value = ChannelsScreenState.TopicMessagesCountUpdate(cache.toList())
            }
            isReturnFromMessagesScreen = false
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
                    addTopicsToCache(channelItem, index + 1)
                } else {
                    var nextIndex = index + 1
                    var isNextChannelItemExist = false
                    while (nextIndex < cache.size && !isNextChannelItemExist) {
                        if (cache[nextIndex] is ChannelDelegateItem) {
                            isNextChannelItemExist = true
                        } else {
                            nextIndex++
                        }
                    }
                    if (!isNextChannelItemExist) {
                        nextIndex = cache.size
                    }
                    cache.subList(index + 1, nextIndex).clear()
                }
                _state.value = ChannelsScreenState.Items(cache.toList())
            }
        }
    }

    fun onTopicClickListener(messagesFilter: MessagesFilter) {
        isReturnFromMessagesScreen = true
        globalRouter.navigateTo(Screens.Messages(messagesFilter))
    }

    private fun updateCache(newItems: List<DelegateAdapterItem>) {
        val newCache = mutableListOf<DelegateAdapterItem>()
        newItems.forEach { newItem ->
            val oldItem = cache.find { oldItem ->
                if (newItem is ChannelDelegateItem && oldItem is ChannelDelegateItem) {
                    val newChannelItem = newItem.content() as ChannelItem
                    val oldChannelItem = oldItem.content() as ChannelItem
                    newChannelItem.channel == oldChannelItem.channel
                } else false
            }

            if (oldItem != null) {
                newCache.add(oldItem)
                val channelItem = oldItem.content() as ChannelItem
                if (!channelItem.isFolded) {
                    val index = cache.indexOf(oldItem)
                    var nextIndex = index + 1
                    var isNextChannelItemExist = false
                    while (nextIndex < cache.size && !isNextChannelItemExist) {
                        if (cache[nextIndex] is ChannelDelegateItem) {
                            isNextChannelItemExist = true
                        } else {
                            nextIndex++
                        }
                    }
                    if (!isNextChannelItemExist) {
                        nextIndex = cache.size
                    }
                    newCache.addAll(cache.subList(index + 1, nextIndex))
                }
            } else if (newItem is ChannelDelegateItem) {
                newCache.add(newItem)
            }
        }

        cache.clear()
        cache.addAll(newCache)
    }

    private suspend fun addTopicsToCache(channelItem: ChannelItem, index: Int = UNDEFINED_INDEX) {
        val topicsResult = getTopicsUseCase(channelItem.channel.channelId)
        if (topicsResult is RepositoryResult.Success) {
            val topics = topicsResult.value.toDelegateItem(channelItem.channel)
            if (index != UNDEFINED_INDEX)
                cache.addAll(index, topics)
            else
                cache.addAll(topics)
        }
        // TODO: process other errors}
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

    private companion object {
        const val UNDEFINED_INDEX = -1
    }
}