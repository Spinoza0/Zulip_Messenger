package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.model.event.ChannelEvent
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.adapter.channels.ChannelDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.channels.TopicDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.model.ChannelItem
import com.spinoza.messenger_tfs.presentation.model.channels.ChannelsPageEffect
import com.spinoza.messenger_tfs.presentation.model.channels.ChannelsPageEvent
import com.spinoza.messenger_tfs.presentation.model.channels.ChannelsPageState
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class ChannelsPageFragmentViewModel(
    private val router: Router,
    private val isAllChannels: Boolean,
    private val getTopicsUseCase: GetTopicsUseCase,
    private val getChannelsUseCase: GetChannelsUseCase,
    private val getTopicUseCase: GetTopicUseCase,
    private val registerEventQueueUseCase: RegisterEventQueueUseCase,
    private val deleteEventQueueUseCase: DeleteEventQueueUseCase,
    private val getChannelEventsUseCase: GetChannelEventsUseCase,
) : ViewModel() {

    val state: StateFlow<ChannelsPageState>
        get() = _state.asStateFlow()

    val effects: SharedFlow<ChannelsPageEffect>
        get() = _effects.asSharedFlow()

    private var channelsFilter = ChannelsFilter(ChannelsFilter.NO_FILTER, !isAllChannels)
    private val _state = MutableStateFlow(ChannelsPageState())
    private val _effects = MutableSharedFlow<ChannelsPageEffect>()
    private val cache = mutableListOf<DelegateAdapterItem>()
    private var eventsQueue = EventsQueue()
    private val channelsQueryState = MutableSharedFlow<ChannelsFilter>()

    init {
        subscribeToChannelsQueryChanges()
        updateTopicsMessageCount()
    }

    fun reduce(event: ChannelsPageEvent) {
        when (event) {
            is ChannelsPageEvent.Ui.Filter -> setChannelsFilter(event.filter)
            is ChannelsPageEvent.Ui.Load -> loadItems()
            is ChannelsPageEvent.Ui.UpdateMessageCount -> updateMessagesCount()
            is ChannelsPageEvent.Ui.OnChannelClick -> onChannelClickListener(event.value)
            is ChannelsPageEvent.Ui.OnTopicClick ->
                router.navigateTo(Screens.Messages(event.messagesFilter))
        }
    }

    override fun onCleared() {
        super.onCleared()
        cache.clear()
        viewModelScope.launch {
            deleteEventQueueUseCase(eventsQueue.queueId)
        }
    }

    private fun setChannelsFilter(newFilter: ChannelsFilter) {
        viewModelScope.launch {
            channelsQueryState.emit(newFilter)
        }
    }

    private fun loadItems() {
        viewModelScope.launch(Dispatchers.Default) {
            _state.emit(state.value.copy(isLoading = true))
            val result = getChannelsUseCase(channelsFilter)
            _state.emit(state.value.copy(isLoading = false))
            when (result) {
                is RepositoryResult.Success -> {
                    updateCacheWithShowedTopicsSaving(result.value.toDelegateItem(isAllChannels))
                    _state.emit(state.value.copy(items = cache.toList()))
                    registerEventQueue()
                }
                is RepositoryResult.Failure -> handleErrors(result)
            }
        }
    }

    private fun updateMessagesCount() {
        viewModelScope.launch(Dispatchers.Default) {
            for (i in 0 until cache.size) {
                if (cache[i] is TopicDelegateItem) {
                    val messagesFilter = cache[i].content() as MessagesFilter
                    val result = getTopicUseCase(messagesFilter)
                    if (result is RepositoryResult.Success) {
                        cache[i] = TopicDelegateItem(messagesFilter.copy(topic = result.value))
                    }
                }
            }
            _state.emit(state.value.copy(items = cache.toList()))
        }
    }

    private fun onChannelClickListener(channelItem: ChannelItem) {
        viewModelScope.launch(Dispatchers.Default) {
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
                _state.emit(state.value.copy(items = cache.toList()))
                updateMessagesCount()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun subscribeToChannelsQueryChanges() {
        channelsQueryState
            .distinctUntilChanged()
            .flatMapLatest { flow { emit(it) } }
            .onEach {
                channelsFilter = it
                loadItems()
            }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    private fun registerEventQueue() {
        viewModelScope.launch {
            when (val queueResult = registerEventQueueUseCase(listOf(EventType.CHANNEL))) {
                is RepositoryResult.Success -> {
                    eventsQueue = queueResult.value
                    handleOnSuccessQueueRegistration()
                }
                is RepositoryResult.Failure -> handleErrors(queueResult)
            }
        }
    }

    private fun handleOnSuccessQueueRegistration() {
        viewModelScope.launch {
            while (true) {
                delay(DELAY_BEFORE_CHANNELS_LIST_UPDATE_INFO)
                val eventResult = getChannelEventsUseCase(eventsQueue)
                if (eventResult is RepositoryResult.Success) {
                    val channels = mutableListOf<Channel>()
                    channels.addAll(cache
                        .filterIsInstance<ChannelDelegateItem>()
                        .filter { channelDelegateItem ->
                            val event = eventResult.value.find { channelEvent ->
                                channelEvent.channel.channelId == channelDelegateItem.id()
                            }
                            if (event == null) true
                            else {
                                event.operation != ChannelEvent.Operation.DELETE
                            }
                        }.map { (it.content() as ChannelItem).channel }
                    )
                    var lastEventId = eventsQueue.lastEventId
                    eventResult.value
                        .filter { it.operation != ChannelEvent.Operation.DELETE }
                        .filter { !channels.contains(it.channel) }
                        .forEach {
                            lastEventId = it.id
                            channels.add(it.channel)
                        }
                    eventsQueue = eventsQueue.copy(lastEventId = lastEventId)
                    updateCacheWithShowedTopicsSaving(channels.toDelegateItem(isAllChannels))
                    _state.emit(state.value.copy(items = cache.toList()))
                }
            }
        }
    }

    private fun updateCacheWithShowedTopicsSaving(newItems: List<DelegateAdapterItem>) {
        val newCache = mutableListOf<DelegateAdapterItem>()
        newItems.forEach { newItem ->
            var oldItem: DelegateAdapterItem? = null
            var oldItemIndex = 0
            if (newItem is ChannelDelegateItem) {
                for (index in cache.indices) {
                    if (cache[index] is ChannelDelegateItem) {
                        val newChannelItem = newItem.content() as ChannelItem
                        val oldChannelItem = cache[index].content() as ChannelItem
                        if (newChannelItem.channel == oldChannelItem.channel) {
                            oldItem = cache[index]
                            oldItemIndex = index
                            break
                        }
                    }
                }
            }

            if (oldItem != null) {
                newCache.add(oldItem)
                val oldChannelItem = oldItem.content() as ChannelItem
                if (!oldChannelItem.isFolded) {
                    var nextIndex = oldItemIndex + 1
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
                    newCache.addAll(cache.subList(oldItemIndex + 1, nextIndex))
                }
            } else if (newItem is ChannelDelegateItem) {
                newCache.add(newItem)
            }
        }

        cache.clear()
        cache.addAll(newCache)
    }

    private suspend fun addTopicsToCache(channelItem: ChannelItem, index: Int = UNDEFINED_INDEX) {
        _state.emit(state.value.copy(isLoading = true))
        val topicsResult = getTopicsUseCase(channelItem.channel)
        _state.emit(state.value.copy(isLoading = false))
        when (topicsResult) {
            is RepositoryResult.Success -> {
                val topics = topicsResult.value.toDelegateItem(channelItem.channel)
                if (index != UNDEFINED_INDEX)
                    cache.addAll(index, topics)
                else
                    cache.addAll(topics)
            }
            is RepositoryResult.Failure -> handleErrors(topicsResult)
        }
    }

    private suspend fun handleErrors(error: RepositoryResult.Failure) {
        when (error) {
            is RepositoryResult.Failure.LoadingChannels -> _effects.emit(
                ChannelsPageEffect.Failure.LoadingChannels(error.channelsFilter, error.value)
            )
            is RepositoryResult.Failure.LoadingChannelTopics -> _effects.emit(
                ChannelsPageEffect.Failure.LoadingChannelTopics(error.channel, error.value)
            )
            is RepositoryResult.Failure.Network ->
                _effects.emit(ChannelsPageEffect.Failure.Network(error.value))
            else -> {}
        }
    }

    private fun updateTopicsMessageCount() {
        viewModelScope.launch {
            delay(DELAY_BEFORE_TOPIC_MESSAGE_COUNT_UPDATE_INFO)
            updateMessagesCount()
        }
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

    private companion object {

        const val UNDEFINED_INDEX = -1
        const val DELAY_BEFORE_CHANNELS_LIST_UPDATE_INFO = 15_000L
        const val DELAY_BEFORE_TOPIC_MESSAGE_COUNT_UPDATE_INFO = 60_000L
    }
}