package com.spinoza.messenger_tfs.presentation.elm

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.cyberfox21.tinkofffintechseminar.di.GlobalDI
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.repository.RepositoryError
import com.spinoza.messenger_tfs.presentation.model.people.PeopleCommand
import com.spinoza.messenger_tfs.presentation.model.people.PeopleEvent
import com.spinoza.messenger_tfs.presentation.utils.EventsQueueProcessor
import com.spinoza.messenger_tfs.presentation.utils.getErrorText
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import vivid.money.elmslie.coroutines.Actor

class PeopleActor(lifecycle: Lifecycle) : Actor<PeopleCommand, PeopleEvent.Internal> {

    private val lifecycleScope = lifecycle.coroutineScope
    private val getUsersByFilterUseCase = GlobalDI.INSTANCE.getUsersByFilterUseCase
    private val getPresenceEventsUseCase = GlobalDI.INSTANCE.getPresenceEventsUseCase

    private var eventsQueue = EventsQueueProcessor(lifecycleScope)
    private val actorFlow = MutableSharedFlow<PeopleEvent.Internal>()
    private val searchQueryState = MutableSharedFlow<String>()
    private var usersCache = mutableListOf<User>()

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            eventsQueue.deleteQueue()
        }
    }

    init {
        lifecycle.addObserver(lifecycleObserver)
        subscribeToSearchQueryChanges()
    }

    override fun execute(command: PeopleCommand): Flow<PeopleEvent.Internal> {
        when (command) {
            is PeopleCommand.Filter -> setFilter(command.filter)
            is PeopleCommand.Load -> loadUsers(command.filter)
        }
        return actorFlow.asSharedFlow()
    }

    private fun setFilter(filter: String) {
        lifecycleScope.launch {
            searchQueryState.emit(filter.trim())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun subscribeToSearchQueryChanges() {
        searchQueryState
            .distinctUntilChanged()
            .debounce(DURATION_MILLIS)
            .flatMapLatest { flow { emit(it) } }
            .onEach { loadUsers(it) }
            .flowOn(Dispatchers.Default)
            .launchIn(lifecycleScope)
    }

    private fun loadUsers(filter: String) {
        lifecycleScope.launch(Dispatchers.Default) {
            actorFlow.emit(PeopleEvent.Internal.Filter(filter))
            getUsersByFilterUseCase(filter).onSuccess {
                usersCache.clear()
                usersCache.addAll(it)
                actorFlow.emit(PeopleEvent.Internal.UsersLoaded(usersCache.toSortedList()))
                eventsQueue.registerQueue(EventType.PRESENCE, ::handleOnSuccessQueueRegistration)
            }.onFailure { error ->
                val peopleEvent = if (error is RepositoryError) {
                    PeopleEvent.Internal.ErrorUserLoading(error.value)
                } else {
                    PeopleEvent.Internal.ErrorNetwork(error.getErrorText())
                }
                actorFlow.emit(peopleEvent)
            }
        }
    }

    private fun handleOnSuccessQueueRegistration() {
        lifecycleScope.launch(Dispatchers.Default) {
            var lastUpdatingTimeStamp = 0L
            while (true) {
                getPresenceEventsUseCase(eventsQueue.queue).onSuccess { events ->
                    var isListChanged = false
                    events.forEach { event ->
                        eventsQueue.queue = eventsQueue.queue.copy(lastEventId = event.id)
                        val index = usersCache.indexOfFirst { it.userId == event.userId }
                        if (index != INDEX_NOT_FOUND) {
                            usersCache[index] = usersCache[index].copy(presence = event.presence)
                            isListChanged = true
                        }
                    }
                    if (isListChanged) {
                        lastUpdatingTimeStamp = System.currentTimeMillis() / MILLIS_IN_SECOND
                        actorFlow.emit(PeopleEvent.Internal.PresencesLoaded(usersCache.toSortedList()))
                    }
                }.onFailure {
                    val currentTimeStamp = System.currentTimeMillis() / MILLIS_IN_SECOND
                    if (currentTimeStamp - lastUpdatingTimeStamp > OFFLINE_TIME) {
                        for (index in 0 until usersCache.size) {
                            usersCache[index] =
                                usersCache[index].copy(presence = User.Presence.OFFLINE)
                        }
                        lastUpdatingTimeStamp = currentTimeStamp
                        actorFlow.emit(PeopleEvent.Internal.PresencesLoaded(usersCache.toSortedList()))
                    }
                }
                delay(DELAY_BEFORE_UPDATE_INFO)
            }
        }
    }

    private fun List<User>.toSortedList(): List<User> {
        val sortedList = ArrayList(this)
        sortedList.sortWith(compareBy<User> { it.presence }.thenBy { it.fullName })
        return sortedList
    }

    private companion object {

        const val DURATION_MILLIS = 300L
        const val DELAY_BEFORE_UPDATE_INFO = 30_000L
        const val INDEX_NOT_FOUND = -1
        const val MILLIS_IN_SECOND = 1000
        const val OFFLINE_TIME = 180
    }
}