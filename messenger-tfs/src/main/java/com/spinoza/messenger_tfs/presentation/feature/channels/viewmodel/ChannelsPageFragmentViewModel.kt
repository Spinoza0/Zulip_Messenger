package com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.di.ChannelIsSubscribed
import com.spinoza.messenger_tfs.di.DispatcherDefault
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.model.event.ChannelEvent
import com.spinoza.messenger_tfs.domain.model.event.EventOperation
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.network.AuthorizationStorage
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.domain.usecase.channels.*
import com.spinoza.messenger_tfs.domain.usecase.event.DeleteEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.GetChannelEventsUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.GetChannelSubscriptionEventsUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.RegisterEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.login.LogInUseCase
import com.spinoza.messenger_tfs.domain.util.EMPTY_STRING
import com.spinoza.messenger_tfs.domain.util.getText
import com.spinoza.messenger_tfs.presentation.adapter.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.feature.channels.adapter.ChannelDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.channels.adapter.CreateChannelDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.channels.adapter.TopicDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.channels.model.*
import com.spinoza.messenger_tfs.presentation.navigation.AppRouter
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import com.spinoza.messenger_tfs.presentation.util.EventsQueueHolder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

class ChannelsPageFragmentViewModel(
    @ChannelIsSubscribed isSubscribed: Boolean,
    private val authorizationStorage: AuthorizationStorage,
    private val router: AppRouter,
    private val logInUseCase: LogInUseCase,
    private val getStoredTopicsUseCase: GetStoredTopicsUseCase,
    private val getTopicsUseCase: GetTopicsUseCase,
    private val getStoredChannelsUseCase: GetStoredChannelsUseCase,
    private val getChannelsUseCase: GetChannelsUseCase,
    private val getChannelSubscriptionStatusUseCase: GetChannelSubscriptionStatusUseCase,
    private val createChannelUseCase: CreateChannelUseCase,
    private val unsubscribeFromChannelUseCase: UnsubscribeFromChannelUseCase,
    private val deleteChannelUseCase: DeleteChannelUseCase,
    private val getTopicUseCase: GetTopicUseCase,
    private val getChannelEventsUseCase: GetChannelEventsUseCase,
    private val getChannelSubscriptionEventsUseCase: GetChannelSubscriptionEventsUseCase,
    registerEventQueueUseCase: RegisterEventQueueUseCase,
    deleteEventQueueUseCase: DeleteEventQueueUseCase,
    @DispatcherDefault private val defaultDispatcher: CoroutineDispatcher,
    customCoroutineScope: CoroutineScope? = null,
) : ViewModel() {

    val state: StateFlow<ChannelsPageScreenState>
        get() = _state.asStateFlow()

    val effects: SharedFlow<ChannelsPageScreenEffect>
        get() = _effects.asSharedFlow()

    private var channelsFilter = ChannelsFilter(EMPTY_STRING, isSubscribed)
    private val _state = MutableStateFlow(ChannelsPageScreenState())
    private val _effects = MutableSharedFlow<ChannelsPageScreenEffect>()
    private val channelsQueryState = MutableSharedFlow<ChannelsFilter>()
    private val cache = mutableListOf<DelegateAdapterItem>()
    private var vmScope = customCoroutineScope ?: viewModelScope
    private var channelEvents =
        EventsQueueHolder(vmScope, registerEventQueueUseCase, deleteEventQueueUseCase)
    private var subscriptionEvents =
        EventsQueueHolder(vmScope, registerEventQueueUseCase, deleteEventQueueUseCase)
    private var channelEventsJob: Job? = null
    private var subscriptionEventsJob: Job? = null
    private var updateMessagesCountJob: Job? = null
    private var updateTopicsCycleJob: Job? = null
    private var isDraggingWithoutScroll = false

    @Volatile
    private var isLoading = false

    init {
        subscribeToChannelsQueryChanges()
    }

    fun accept(event: ChannelsPageScreenEvent) {
        when (event) {
            is ChannelsPageScreenEvent.Ui.Filter -> setChannelsFilter(event.filter)
            is ChannelsPageScreenEvent.Ui.OnScrolled -> isDraggingWithoutScroll = false
            is ChannelsPageScreenEvent.Ui.ScrollStateDragging -> isDraggingWithoutScroll = true
            is ChannelsPageScreenEvent.Ui.ScrollStateIdle -> scrollStateIdleHandler(event)
            is ChannelsPageScreenEvent.Ui.Load -> loadItems()
            is ChannelsPageScreenEvent.Ui.OnChannelClick -> onChannelClickListener(event.value)
            is ChannelsPageScreenEvent.Ui.OpenMessagesScreen ->
                router.navigateTo(Screens.Messages(event.messagesFilter))

            is ChannelsPageScreenEvent.Ui.CheckLoginStatus -> checkLoginStatus()
            is ChannelsPageScreenEvent.Ui.ShowChannelMenu -> showChannelMenu(event)
            is ChannelsPageScreenEvent.Ui.CreateChannel ->
                createChannel(event.name, event.description)

            is ChannelsPageScreenEvent.Ui.SubscribeToChannel ->
                createChannel(event.name, EMPTY_STRING)

            is ChannelsPageScreenEvent.Ui.UnsubscribeFromChannel ->
                unsubscribeFromChannel(event.name)

            is ChannelsPageScreenEvent.Ui.DeleteChannel -> deleteChannel(event.channelId)
            is ChannelsPageScreenEvent.Ui.OnResume -> {
                startUpdateTopicsCycle()
                subscribeOnEvents()
            }

            is ChannelsPageScreenEvent.Ui.OnPause -> {
                unsubscribeFromEvents()
                stopUpdateMessagesCountJob()
                updateTopicsCycleJob?.cancel()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cache.clear()
    }

    private fun checkLoginStatus() {
        if (!authorizationStorage.isUserLoggedIn()) {
            if (authorizationStorage.isAuthorizationDataExisted()) {
                logIn()
            } else {
                openLoginScreen()
            }
        }
    }

    private fun logIn() {
        vmScope.launch {
            logInUseCase(
                authorizationStorage.getEmail(), authorizationStorage.getPassword()
            ).onFailure { error ->
                if (error is RepositoryError) {
                    openLoginScreen()
                } else {
                    _effects.emit(ChannelsPageScreenEffect.Failure.Network(error.getText()))
                }
            }
        }
    }

    private fun openLoginScreen() {
        router.replaceScreen(Screens.Login())
    }

    private fun setChannelsFilter(newFilter: ChannelsFilter) {
        vmScope.launch {
            channelsQueryState.emit(newFilter)
        }
    }

    private fun loadItems() {
        if (isLoading) return
        vmScope.launch(defaultDispatcher) {
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

    private fun scrollStateIdleHandler(event: ChannelsPageScreenEvent.Ui.ScrollStateIdle) {
        if (isDraggingWithoutScroll) {
            isDraggingWithoutScroll = false
            if (!event.canScrollUp || !event.canScrollDown) {
                loadItems()
            }
        }
    }

    private fun updateMessagesCount() {
        stopUpdateMessagesCountJob()
        updateMessagesCountJob = vmScope.launch(defaultDispatcher) {
            for (i in 0 until cache.size) {
                if (!isActive) return@launch
                runCatching {
                    val delegateItem = cache[i]
                    if (delegateItem is TopicDelegateItem) updateTopicDelegateItem(delegateItem, i)
                }
            }
        }
    }

    private fun createChannel(name: CharSequence?, description: CharSequence?) {
        val trimmedName = name.toString().trim()
        if (trimmedName.isNotBlank()) vmScope.launch {
            createChannelUseCase(trimmedName, description.toString().trim()).onFailure {
                handleErrors(it)
            }
        }
    }

    private fun unsubscribeFromChannel(name: String) {
        vmScope.launch {
            unsubscribeFromChannelUseCase(name).onFailure {
                handleErrors(it)
            }
        }
    }

    private fun deleteChannel(channelId: Long) {
        vmScope.launch {
            deleteChannelUseCase(channelId).onFailure {
                handleErrors(it)
            }
        }
    }

    private fun showChannelMenu(event: ChannelsPageScreenEvent.Ui.ShowChannelMenu) {
        vmScope.launch {
            getChannelSubscriptionStatusUseCase(event.channelItem.channel.channelId)
                .onSuccess { isSubscribed ->
                    _effects.emit(
                        ChannelsPageScreenEffect.ShowChannelMenu(
                            event.channelItem,
                            !isSubscribed, isSubscribed, authorizationStorage.isAdmin()
                        )
                    )
                }.onFailure {
                    handleErrors(it)
                }
        }
    }

    private suspend fun updateTopicDelegateItem(delegateItem: DelegateAdapterItem, itemIndex: Int) {
        val messagesFilter = delegateItem.content() as MessagesFilter
        getTopicUseCase(messagesFilter).onSuccess { newTopic ->
            if (messagesFilter.topic.messageCount != newTopic.messageCount) {
                cache[itemIndex] = TopicDelegateItem(messagesFilter.copy(topic = newTopic))
                runCatching { _state.emit(state.value.copy(items = cache.groupByChannel())) }
            }
        }
    }

    private fun onChannelClickListener(channelItem: ChannelItem) {
        vmScope.launch(defaultDispatcher) {
            val oldChannelDelegateItem = cache.find { delegateAdapterItem ->
                if (delegateAdapterItem is ChannelDelegateItem) {
                    val item = delegateAdapterItem.content() as ChannelItem
                    item.channel.channelId == channelItem.channel.channelId
                } else false
            }

            if (oldChannelDelegateItem != null) {
                val index = cache.indexOf(oldChannelDelegateItem)
                val oldChannelItem = oldChannelDelegateItem.content() as ChannelItem
                stopUpdateMessagesCountJob()
                val newChannelDelegateItem = ChannelDelegateItem(
                    oldChannelItem.copy(isFolded = !oldChannelItem.isFolded)
                )
                cache[index] = newChannelDelegateItem
                if (oldChannelItem.isFolded) {
                    unfoldChannel(channelItem)
                } else {
                    runCatching { _state.emit(state.value.copy(items = cache.groupByChannel())) }
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
            .flowOn(defaultDispatcher)
            .launchIn(vmScope)
    }

    private fun handleOnChannelEventsRegistration() {
        channelEventsJob?.cancel()
        channelEventsJob = vmScope.launch(defaultDispatcher) {
            while (isActive) {
                delay(DELAY_BEFORE_CHANNEL_EVENTS)
                getChannelEventsUseCase(channelEvents.queue, channelsFilter).onSuccess {
                    eventsHandler(channelEvents, it)
                }
            }
        }
    }

    private fun handleOnChannelSubscriptionEventsRegistration() {
        subscriptionEventsJob?.cancel()
        subscriptionEventsJob = vmScope.launch(defaultDispatcher) {
            while (isActive) {
                delay(DELAY_BEFORE_CHANNEL_SUBSCRIPTION_EVENTS)
                getChannelSubscriptionEventsUseCase(subscriptionEvents.queue, channelsFilter)
                    .onSuccess {
                        eventsHandler(subscriptionEvents, it)
                    }
            }
        }
    }

    private suspend fun eventsHandler(
        eventsQueueHolder: EventsQueueHolder,
        events: List<ChannelEvent>,
    ) {
        val newChannels = mutableListOf<Channel>()
        newChannels.addAll(cache
            .filterIsInstance<ChannelDelegateItem>()
            .filter { channelDelegateItem ->
                val event = events.find { channelEvent ->
                    channelEvent.channel.channelId == channelDelegateItem.id()
                }
                event == null ||
                        (event.operation != EventOperation.DELETE &&
                                event.operation != EventOperation.REMOVE)
            }.map { (it.content() as ChannelItem).channel }
        )
        var lastEventId = eventsQueueHolder.queue.lastEventId
        events
            .filter {
                it.operation != EventOperation.DELETE && it.operation != EventOperation.REMOVE
            }
            .filter { !newChannels.contains(it.channel) }
            .forEach {
                lastEventId = it.id
                newChannels.add(it.channel)
            }
        eventsQueueHolder.queue = eventsQueueHolder.queue.copy(lastEventId = lastEventId)
        updateChannelsList(newChannels)
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
        runCatching { _state.emit(state.value.copy(items = cache.groupByChannel())) }
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
            val oldChannelDelegateItem = newCache.find { storedDelegateItem ->
                storedDelegateItem is ChannelDelegateItem &&
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
        runCatching { _state.emit(state.value.copy(items = cache.groupByChannel())) }
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
            ChannelsPageScreenEffect.Failure.Network(error.getText())
        }
        _effects.emit(channelsPageScreenEffect)
    }

    private fun startUpdateTopicsCycle() {
        updateTopicsCycleJob = vmScope.launch {
            while (isActive) {
                updateMessagesCount()
                delay(DELAY_BEFORE_TOPIC_MESSAGE_COUNT_UPDATE_INFO)
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
        result.add(CreateChannelDelegateItem())
        return result
    }

    private fun subscribeOnEvents() {
        channelEvents.registerQueue(listOf(EventType.CHANNEL), ::handleOnChannelEventsRegistration)
        subscriptionEvents.registerQueue(
            listOf(EventType.CHANNEL_SUBSCRIPTION), ::handleOnChannelSubscriptionEventsRegistration
        )
    }

    private fun unsubscribeFromEvents() {
        channelEventsJob?.let {
            it.cancel()
            channelEventsJob = null
        }
        subscriptionEventsJob?.let {
            it.cancel()
            subscriptionEventsJob = null
        }
        vmScope.launch {
            channelEvents.deleteQueue()
            subscriptionEvents.deleteQueue()
        }
    }

    private companion object {

        const val DELAY_BEFORE_CHANNEL_EVENTS = 3_000L
        const val DELAY_BEFORE_CHANNEL_SUBSCRIPTION_EVENTS = 5_000L
        const val DELAY_BEFORE_TOPIC_MESSAGE_COUNT_UPDATE_INFO = 60_000L
    }
}