package com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.model.event.ChannelEvent
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.repository.RepositoryError
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.feature.app.adapter.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.feature.app.utils.EventsQueueHolder
import com.spinoza.messenger_tfs.presentation.feature.app.utils.getErrorText
import com.spinoza.messenger_tfs.presentation.feature.channels.adapter.ChannelDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.channels.adapter.TopicDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.channels.model.ChannelItem
import com.spinoza.messenger_tfs.presentation.feature.channels.model.ChannelsPageScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.channels.model.ChannelsPageScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.channels.model.ChannelsPageScreenState
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class ChannelsPageFragmentViewModel(
    isSubscribed: Boolean,
    private val router: Router,
    private val getTopicsUseCase: GetTopicsUseCase,
    private val getChannelsUseCase: GetChannelsUseCase,
    private val getTopicUseCase: GetTopicUseCase,
    private val getChannelEventsUseCase: GetChannelEventsUseCase,
    registerEventQueueUseCase: RegisterEventQueueUseCase,
    deleteEventQueueUseCase: DeleteEventQueueUseCase,
) : ViewModel() {

    val state: StateFlow<ChannelsPageScreenState>
        get() = _state.asStateFlow()

    val effects: SharedFlow<ChannelsPageScreenEffect>
        get() = _effects.asSharedFlow()

    private var channelsFilter = ChannelsFilter(EMPTY_NAME, isSubscribed)
    private val _state = MutableStateFlow(ChannelsPageScreenState())
    private val _effects = MutableSharedFlow<ChannelsPageScreenEffect>()
    private val channelsQueryState = MutableSharedFlow<ChannelsFilter>()
    private val cache = mutableListOf<DelegateAdapterItem>()
    private var eventsQueue =
        EventsQueueHolder(viewModelScope, registerEventQueueUseCase, deleteEventQueueUseCase)
    private var updateMessagesCountJob: Job? = null

    init {
        subscribeToChannelsQueryChanges()
        updateTopicsMessageCount()
    }

    fun accept(event: ChannelsPageScreenEvent) {
        when (event) {
            is ChannelsPageScreenEvent.Ui.Filter -> setChannelsFilter(event.filter)
            is ChannelsPageScreenEvent.Ui.Load -> loadItems()
            is ChannelsPageScreenEvent.Ui.UpdateMessageCount -> updateMessagesCount()
            is ChannelsPageScreenEvent.Ui.OnChannelClick -> onChannelClickListener(event.value)
            is ChannelsPageScreenEvent.Ui.OnTopicClick ->
                router.navigateTo(Screens.Messages(event.messagesFilter))
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopUpdateMessagesCountJob()
        cache.clear()
        viewModelScope.launch {
            eventsQueue.deleteQueue()
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
            result.onSuccess {
                updateCacheWithShowedTopicsSaving(it.toDelegateItem())
                _state.emit(state.value.copy(items = cache.toList()))
                eventsQueue.registerQueue(EventType.CHANNEL, ::handleOnSuccessQueueRegistration)
            }.onFailure {
                handleErrors(it)
            }
        }
    }

    private fun updateMessagesCount() {
        updateMessagesCountJob = viewModelScope.launch(Dispatchers.Default) {
            for (i in 0 until cache.size) {
                if (!isActive) return@launch
                runCatching {
                    val delegateItem = cache[i]
                    if (delegateItem is TopicDelegateItem) updateTopicDelegateItem(delegateItem, i)
                }
            }
        }
    }

    private suspend fun updateTopicDelegateItem(delegateItem: DelegateAdapterItem, itemIndex: Int) {
        val messagesFilter = delegateItem.content() as MessagesFilter
        getTopicUseCase(messagesFilter).onSuccess { newTopic ->
            if (messagesFilter.topic.messageCount != newTopic.messageCount) {
                cache[itemIndex] = TopicDelegateItem(messagesFilter.copy(topic = newTopic))
                _state.emit(state.value.copy(items = cache.toList()))
            }
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
                if (!oldChannelItem.isFolded) {
                    stopUpdateMessagesCountJob()
                }
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

    private fun stopUpdateMessagesCountJob() {
        updateMessagesCountJob?.let {
            it.cancel()
            updateMessagesCountJob = null
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

    private fun handleOnSuccessQueueRegistration() {
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(DELAY_BEFORE_CHANNELS_LIST_UPDATE_INFO)
                getChannelEventsUseCase(eventsQueue.queue).onSuccess { events ->
                    val channels = mutableListOf<Channel>()
                    channels.addAll(cache
                        .filterIsInstance<ChannelDelegateItem>()
                        .filter { channelDelegateItem ->
                            val event = events.find { channelEvent ->
                                channelEvent.channel.channelId == channelDelegateItem.id()
                            }
                            if (event == null) true
                            else {
                                event.operation != ChannelEvent.Operation.DELETE
                            }
                        }.map { (it.content() as ChannelItem).channel }
                    )
                    var lastEventId = eventsQueue.queue.lastEventId
                    events
                        .filter { it.operation != ChannelEvent.Operation.DELETE }
                        .filter { !channels.contains(it.channel) }
                        .forEach {
                            lastEventId = it.id
                            channels.add(it.channel)
                        }
                    eventsQueue.queue = eventsQueue.queue.copy(lastEventId = lastEventId)
                    updateCacheWithShowedTopicsSaving(channels.toDelegateItem())
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
        topicsResult.onSuccess {
            val topics = it.toDelegateItem(channelItem.channel)
            if (index != UNDEFINED_INDEX)
                cache.addAll(index, topics)
            else
                cache.addAll(topics)
        }.onFailure {
            handleErrors(it)
        }
    }

    private suspend fun handleErrors(error: Throwable) {
        val channelsPageScreenEffect = if (error is RepositoryError) {
            ChannelsPageScreenEffect.Failure.Error(error.value)
        } else {
            ChannelsPageScreenEffect.Failure.Network(error.getErrorText())
        }
        _effects.emit(channelsPageScreenEffect)
    }

    private fun updateTopicsMessageCount() {
        viewModelScope.launch {
            while (isActive) {
                delay(DELAY_BEFORE_TOPIC_MESSAGE_COUNT_UPDATE_INFO)
                updateMessagesCount()
            }
        }
    }

    private fun Channel.toDelegateItem(): ChannelDelegateItem {
        return ChannelDelegateItem(ChannelItem(this, channelsFilter.isSubscribed, true))
    }

    private fun List<Channel>.toDelegateItem(): List<ChannelDelegateItem> {
        return map { it.toDelegateItem() }
    }

    private fun Topic.toDelegateItem(channel: Channel): TopicDelegateItem {
        return TopicDelegateItem(MessagesFilter(channel, this))
    }

    private fun List<Topic>.toDelegateItem(channel: Channel): List<TopicDelegateItem> {
        return map { it.toDelegateItem(channel) }
    }

    private companion object {

        const val UNDEFINED_INDEX = -1
        const val EMPTY_NAME = ""
        const val DELAY_BEFORE_CHANNELS_LIST_UPDATE_INFO = 15_000L
        const val DELAY_BEFORE_TOPIC_MESSAGE_COUNT_UPDATE_INFO = 60_000L
    }
}