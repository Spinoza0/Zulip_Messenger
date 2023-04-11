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
    private val searchQueryState = MutableSharedFlow<String>()
    private var usersCache = mutableListOf<User>()
    private var usersFilter = ""
    private var isPresencesChanged = false

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            eventsQueue.deleteQueue()
        }
    }

    init {
        lifecycle.addObserver(lifecycleObserver)
        subscribeToSearchQueryChanges()
    }

    override fun execute(command: PeopleCommand): Flow<PeopleEvent.Internal> = flow {
        val event = when (command) {
            is PeopleCommand.SetNewFilter -> setNewFilter(command.filter.trim())
            is PeopleCommand.GetFilteredList ->
                PeopleEvent.Internal.UsersLoaded(usersCache.toSortedList(usersFilter))
            is PeopleCommand.Load -> {
                loadUsers()
                setNewFilter(command.filter)
            }
            is PeopleCommand.GetEvent -> if (isPresencesChanged) {
                isPresencesChanged = false
                PeopleEvent.Internal.UsersLoaded(usersCache.toSortedList(usersFilter))
            } else {
                delay(DELAY_BEFORE_UPDATE_INFO)
                PeopleEvent.Internal.EmptyQueueEvent
            }
        }
        emit(event)
    }

    private suspend fun setNewFilter(filter: String): PeopleEvent.Internal {
        searchQueryState.emit(filter)
        delay(DURATION_MILLIS_CHECK_FILTER)
        if (filter == usersFilter) {
            return PeopleEvent.Internal.FilterChanged
        }
        return PeopleEvent.Internal.Idle
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun subscribeToSearchQueryChanges() {
        searchQueryState
            .distinctUntilChanged()
            .debounce(DURATION_MILLIS_SET_FILTER)
            .flatMapLatest { flow { emit(it) } }
            .onEach { usersFilter = it }
            .flowOn(Dispatchers.Default)
            .launchIn(lifecycleScope)
    }

    private suspend fun loadUsers(): PeopleEvent.Internal {
        var event: PeopleEvent.Internal = PeopleEvent.Internal.Idle
        getUsersByFilterUseCase(NO_FILTER).onSuccess {
            usersCache.clear()
            usersCache.addAll(it)
            event = PeopleEvent.Internal.UsersLoaded(usersCache.toSortedList())
            eventsQueue.registerQueue(EventType.PRESENCE, ::handleOnSuccessQueueRegistration)
        }.onFailure { error ->
            event = if (error is RepositoryError) {
                PeopleEvent.Internal.ErrorUserLoading(error.value)
            } else {
                PeopleEvent.Internal.ErrorNetwork(error.getErrorText())
            }
        }
        return event
    }

    private fun handleOnSuccessQueueRegistration() {
        lifecycleScope.launch(Dispatchers.Default) {
            var lastUpdatingTimeStamp = 0L
            while (true) {
                getPresenceEventsUseCase(eventsQueue.queue).onSuccess { events ->
                    events.forEach { event ->
                        eventsQueue.queue = eventsQueue.queue.copy(lastEventId = event.id)
                        val index = usersCache.indexOfFirst { it.userId == event.userId }
                        if (index != INDEX_NOT_FOUND) {
                            usersCache[index] = usersCache[index].copy(presence = event.presence)
                            isPresencesChanged = true
                        }
                    }
                    if (isPresencesChanged) {
                        lastUpdatingTimeStamp = System.currentTimeMillis() / MILLIS_IN_SECOND
                    }
                }.onFailure {
                    val currentTimeStamp = System.currentTimeMillis() / MILLIS_IN_SECOND
                    if (currentTimeStamp - lastUpdatingTimeStamp > OFFLINE_TIME) {
                        for (index in 0 until usersCache.size) {
                            usersCache[index] =
                                usersCache[index].copy(presence = User.Presence.OFFLINE)
                        }
                        lastUpdatingTimeStamp = currentTimeStamp
                        isPresencesChanged = true
                    }
                }
                delay(DELAY_BEFORE_UPDATE_INFO)
            }
        }
    }

    private suspend fun List<User>.toSortedList(filter: String = NO_FILTER): List<User> =
        withContext(Dispatchers.Default) {
            val sortedList = ArrayList(this@toSortedList)
            sortedList.sortWith(compareBy<User> { it.presence }.thenBy { it.fullName })
            if (filter.isBlank()) {
                sortedList
            } else {
                sortedList.filter {
                    it.fullName.contains(filter, true) || it.email.contains(filter, true)
                }
            }
        }

    private companion object {

        const val NO_FILTER = ""
        const val DURATION_MILLIS_SET_FILTER = 300L
        const val DURATION_MILLIS_CHECK_FILTER = 400L
        const val DELAY_BEFORE_UPDATE_INFO = 30_000L
        const val INDEX_NOT_FOUND = -1
        const val MILLIS_IN_SECOND = 1000
        const val OFFLINE_TIME = 180
    }
}