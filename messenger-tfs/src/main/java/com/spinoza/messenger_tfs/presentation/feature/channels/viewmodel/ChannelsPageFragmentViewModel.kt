package com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.di.ChannelIsSubscribed
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.model.event.ChannelEvent
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.repository.RepositoryError
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.feature.app.adapter.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.feature.app.utils.EventsQueueHolder
import com.spinoza.messenger_tfs.presentation.feature.app.utils.getErrorText
import com.spinoza.messenger_tfs.presentation.feature.channels.adapter.ChannelDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.channels.adapter.TopicDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.channels.model.*
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

class ChannelsPageFragmentViewModel @Inject constructor(
    @ChannelIsSubscribed isSubscribed: Boolean,
    private val router: Router,
    private val getStoredTopicsUseCase: GetStoredTopicsUseCase,
    private val getTopicsUseCase: GetTopicsUseCase,
    private val getStoredChannelsUseCase: GetStoredChannelsUseCase,
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

    @Volatile
    private var isLoading = false

    init {
        subscribeToChannelsQueryChanges()
        updateTopicsMessageCount()
    }

    fun accept(event: ChannelsPageScreenEvent) {
        when (event) {
            is ChannelsPageScreenEvent.Ui.Filter -> setChannelsFilter(event.filter)
            is ChannelsPageScreenEvent.Ui.OnScrolled -> onScrolled(event)
            is ChannelsPageScreenEvent.Ui.Load -> loadItems()
            is ChannelsPageScreenEvent.Ui.UpdateMessageCount -> updateMessagesCount()
            is ChannelsPageScreenEvent.Ui.OnChannelClick -> onChannelClickListener(event.value)
            is ChannelsPageScreenEvent.Ui.OnTopicClick ->
                router.navigateTo(Screens.Messages(event.messagesFilter))
            is ChannelsPageScreenEvent.Ui.RegisterEventQueue -> registerEventQueue()
            is ChannelsPageScreenEvent.Ui.DeleteEventQueue -> deleteEventQueue()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopUpdateMessagesCountJob()
        cache.clear()
    }

    private fun setChannelsFilter(newFilter: ChannelsFilter) {
        viewModelScope.launch {
            channelsQueryState.emit(newFilter)
        }
    }

    private fun loadItems() {
        if (isLoading) return
        viewModelScope.launch(Dispatchers.Default) {
            isLoading = true
            stopUpdateMessagesCountJob()
            var storedChannels = emptyList<Channel>()
            getStoredChannelsUseCase(channelsFilter).onSuccess { channels ->
                storedChannels = channels
                if (storedChannels.isNotEmpty()) {
                    updateChannelsList(storedChannels)
                }
            }
            if (storedChannels.isEmpty()) {
                _state.emit(state.value.copy(isLoading = true))
            }
            val result = getChannelsUseCase(channelsFilter)
            if (storedChannels.isEmpty()) {
                _state.emit(state.value.copy(isLoading = false))
            }
            isLoading = false
            result.onSuccess { newChannels ->
                updateChannelsList(newChannels)
            }.onFailure {
                handleErrors(it)
            }
        }
    }

    private fun onScrolled(event: ChannelsPageScreenEvent.Ui.OnScrolled) {
        if ((!event.canScrollUp && event.dy <= ChannelsPageScreenEvent.DIRECTION_UP) ||
            (!event.canScrollDown && event.dy >= ChannelsPageScreenEvent.DIRECTION_DOWN)
        ) {
            loadItems()
        }
    }

    private fun updateMessagesCount() {
        stopUpdateMessagesCountJob()
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
                _state.emit(state.value.copy(items = cache.groupByChannel()))
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
                    unfoldChannel(channelItem)
                } else {
                    _state.emit(state.value.copy(items = cache.groupByChannel()))
                }
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
                getChannelEventsUseCase(eventsQueue.queue, channelsFilter).onSuccess { events ->
                    val newChannels = mutableListOf<Channel>()
                    newChannels.addAll(cache
                        .filterIsInstance<ChannelDelegateItem>()
                        .filter { channelDelegateItem ->
                            val event = events.find { channelEvent ->
                                channelEvent.channel.channelId == channelDelegateItem.id()
                            }
                            event == null || (event.operation != ChannelEvent.Operation.DELETE)
                        }.map { (it.content() as ChannelItem).channel }
                    )
                    var lastEventId = eventsQueue.queue.lastEventId
                    events
                        .filter { it.operation != ChannelEvent.Operation.DELETE }
                        .filter { !newChannels.contains(it.channel) }
                        .forEach {
                            lastEventId = it.id
                            newChannels.add(it.channel)
                        }
                    eventsQueue.queue = eventsQueue.queue.copy(lastEventId = lastEventId)
                    updateChannelsList(newChannels)
                }
            }
        }
    }

    private suspend fun unfoldChannel(channelItem: ChannelItem) {
        var storedTopics = emptyList<Topic>()
        getStoredTopicsUseCase(channelItem.channel).onSuccess { topics ->
            storedTopics = topics
            if (storedTopics.isNotEmpty()) {
                updateChannelTopicsList(storedTopics, channelItem.channel)
            }
        }
        if (storedTopics.isEmpty()) {
            _state.emit(state.value.copy(isLoading = true))
        }
        val topicsResult = getTopicsUseCase(channelItem.channel)
        if (storedTopics.isEmpty()) {
            _state.emit(state.value.copy(isLoading = false))
        }
        topicsResult.onSuccess { topics ->
            updateChannelTopicsList(topics, channelItem.channel)
        }.onFailure {
            handleErrors(it)
        }
    }

    private suspend fun updateChannelsList(newChannels: List<Channel>) {
        val newCache = mutableListOf<DelegateAdapterItem>()
        updateStoredChannels(newCache, newChannels)
        addNewChannels(newCache, newChannels)
        cache.clear()
        cache.addAll(newCache)
        _state.emit(state.value.copy(items = cache.groupByChannel()))
        updateMessagesCount()
    }

    private fun updateStoredChannels(
        newCache: MutableList<DelegateAdapterItem>,
        newChannels: List<Channel>,
    ) {
        cache.forEach { storedDelegateItem ->
            if (storedDelegateItem is TopicDelegateItem) {
                newCache.add(storedDelegateItem)
            } else if (storedDelegateItem is ChannelDelegateItem) {
                val storedChannelItem = storedDelegateItem.content() as ChannelItem
                val newChannel = newChannels.find {
                    it.channelId == storedChannelItem.channel.channelId
                }
                if (newChannel != null) {
                    val channelItem = ChannelItem(newChannel, storedChannelItem.isFolded)
                    newCache.add(ChannelDelegateItem(channelItem))
                }
            }
        }
    }

    private fun addNewChannels(
        newCache: MutableList<DelegateAdapterItem>,
        newChannels: List<Channel>,
    ) {
        newChannels.forEach { newChannel ->
            val oldChannelDelegateItem = cache.find { storedDelegateItem ->
                storedDelegateItem !is ChannelDelegateItem ||
                        (storedDelegateItem.content() as ChannelItem)
                            .channel.channelId == newChannel.channelId
            }
            if (oldChannelDelegateItem == null) {
                newCache.add(ChannelDelegateItem(ChannelItem(newChannel, true)))
            }
        }
    }

    private suspend fun updateChannelTopicsList(newTopics: List<Topic>, channel: Channel) {
        removedStoredChannelTopics(channel)
        addNewTopics(newTopics, channel)
        _state.emit(state.value.copy(items = cache.groupByChannel()))
    }

    private fun removedStoredChannelTopics(channel: Channel) {
        cache.retainAll(cache.filter { delegateItem ->
            if (delegateItem is TopicDelegateItem) {
                (delegateItem.content() as MessagesFilter).topic.channelId != channel.channelId
            } else true
        })
    }

    private fun addNewTopics(newTopics: List<Topic>, channel: Channel) {
        newTopics.forEach { newTopic ->
            val oldTopicDelegateItem = cache.find { storedDelegateItem ->
                if (storedDelegateItem is ChannelDelegateItem) false
                else {
                    val oldTopic = (storedDelegateItem.content() as MessagesFilter).topic
                    oldTopic.name == newTopic.name && oldTopic.channelId == newTopic.channelId
                }
            }
            if (oldTopicDelegateItem == null) {
                cache.add(TopicDelegateItem(MessagesFilter(channel, newTopic)))
            }
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

    private fun List<DelegateAdapterItem>.groupByChannel(): List<DelegateAdapterItem> {
        val result = mutableListOf<DelegateAdapterItem>()
        val channels = TreeSet<ChannelDelegateItem> { o1, o2 ->
            (o1.content() as ChannelItem).channel.name.compareTo(
                (o2.content() as ChannelItem).channel.name
            )
        }
        forEach { if (it is ChannelDelegateItem) channels.add(it) }
        channels.forEach { channelDelegateItem ->
            result.add(channelDelegateItem)
            if (!(channelDelegateItem.content() as ChannelItem).isFolded) {
                val channelTopics = filter {
                    it is TopicDelegateItem && (it.content() as MessagesFilter).topic.channelId ==
                            (channelDelegateItem.content() as ChannelItem).channel.channelId
                }.sortedWith(compareBy { (it.content() as MessagesFilter).topic.name })
                result.addAll(channelTopics)
            }
        }
        return result
    }

    private fun registerEventQueue() {
        viewModelScope.launch {
            eventsQueue.registerQueue(listOf(EventType.CHANNEL), ::handleOnSuccessQueueRegistration)
        }
    }

    private fun deleteEventQueue() {
        viewModelScope.launch {
            eventsQueue.deleteQueue()
        }
    }

    private companion object {

        const val EMPTY_NAME = ""
        const val DELAY_BEFORE_CHANNELS_LIST_UPDATE_INFO = 15_000L
        const val DELAY_BEFORE_TOPIC_MESSAGE_COUNT_UPDATE_INFO = 60_000L
    }
}