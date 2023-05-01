package com.spinoza.messenger_tfs.presentation.feature.people

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.spinoza.messenger_tfs.di.DispatcherDefault
import com.spinoza.messenger_tfs.domain.model.RepositoryError
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.usecase.event.GetPresenceEventsUseCase
import com.spinoza.messenger_tfs.domain.usecase.people.GetAllUsersUseCase
import com.spinoza.messenger_tfs.domain.util.isContainingWords
import com.spinoza.messenger_tfs.domain.util.splitToWords
import com.spinoza.messenger_tfs.presentation.feature.people.model.PeopleScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.people.model.PeopleScreenEvent
import com.spinoza.messenger_tfs.presentation.util.EventsQueueHolder
import com.spinoza.messenger_tfs.presentation.util.getErrorText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vivid.money.elmslie.coroutines.Actor
import javax.inject.Inject

class PeopleActor @Inject constructor(
    lifecycle: Lifecycle,
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val getPresenceEventsUseCase: GetPresenceEventsUseCase,
    private val eventsQueue: EventsQueueHolder,
    @DispatcherDefault private val defaultDispatcher: CoroutineDispatcher,
) : Actor<PeopleScreenCommand, PeopleScreenEvent.Internal> {

    private val lifecycleScope = lifecycle.coroutineScope
    private val searchQueryState = MutableSharedFlow<String>()
    private var usersCache = mutableListOf<User>()
    private var isUsersCacheChanged = false
    private var usersFilter = ""

    @Volatile
    private var isLoading = false

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            lifecycleScope.launch {
                eventsQueue.deleteQueue()
            }
        }
    }

    init {
        lifecycle.addObserver(lifecycleObserver)
        subscribeToSearchQueryChanges()
    }

    override fun execute(command: PeopleScreenCommand): Flow<PeopleScreenEvent.Internal> = flow {
        val event = when (command) {
            is PeopleScreenCommand.SetNewFilter -> setNewFilter(command.filter.trim())
            is PeopleScreenCommand.GetFilteredList ->
                if (usersCache.isNotEmpty()) {
                    PeopleScreenEvent.Internal.UsersLoaded(usersCache.toSortedList(usersFilter))
                } else {
                    loadUsers()
                }

            is PeopleScreenCommand.Load -> loadUsers()
            is PeopleScreenCommand.GetEvent -> if (isUsersCacheChanged) {
                isUsersCacheChanged = false
                PeopleScreenEvent.Internal.EventFromQueue(usersCache.toSortedList(usersFilter))
            } else {
                delay(DELAY_BEFORE_UPDATE_INFO)
                PeopleScreenEvent.Internal.EmptyQueueEvent
            }
        }
        emit(event)
    }

    private suspend fun setNewFilter(filter: String): PeopleScreenEvent.Internal {
        searchQueryState.emit(filter)
        delay(DELAY_BEFORE_CHECK_FILTER)
        if (filter == usersFilter) {
            return PeopleScreenEvent.Internal.FilterChanged
        }
        return PeopleScreenEvent.Internal.Idle
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun subscribeToSearchQueryChanges() {
        searchQueryState
            .distinctUntilChanged()
            .debounce(DELAY_BEFORE_SET_FILTER)
            .flatMapLatest { flow { emit(it) } }
            .onEach { usersFilter = it }
            .flowOn(defaultDispatcher)
            .launchIn(lifecycleScope)
    }

    private suspend fun loadUsers(): PeopleScreenEvent.Internal {
        if (isLoading) return getIdleEvent()
        isLoading = true
        var event: PeopleScreenEvent.Internal = PeopleScreenEvent.Internal.Idle
        getAllUsersUseCase().onSuccess {
            usersCache.clear()
            usersCache.addAll(it)
            event = PeopleScreenEvent.Internal.UsersLoaded(usersCache.toSortedList(usersFilter))
            eventsQueue
                .registerQueue(listOf(EventType.PRESENCE), ::handleOnSuccessQueueRegistration)
        }.onFailure { error ->
            event = if (error is RepositoryError) {
                PeopleScreenEvent.Internal.ErrorUserLoading(error.value)
            } else {
                PeopleScreenEvent.Internal.ErrorNetwork(error.getErrorText())
            }
        }
        isLoading = false
        return event
    }

    private fun handleOnSuccessQueueRegistration() {
        lifecycleScope.launch(defaultDispatcher) {
            var lastUpdatingTimeStamp = 0L
            while (isActive) {
                getPresenceEventsUseCase(eventsQueue.queue).onSuccess { events ->
                    events.forEach { event ->
                        eventsQueue.queue = eventsQueue.queue.copy(lastEventId = event.id)
                        val index = usersCache.indexOfFirst { it.userId == event.userId }
                        if (index != INDEX_NOT_FOUND) {
                            usersCache[index] = usersCache[index].copy(presence = event.presence)
                            isUsersCacheChanged = true
                        }
                    }
                    if (isUsersCacheChanged) {
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
                        isUsersCacheChanged = true
                    }
                }
                delay(DELAY_BEFORE_UPDATE_INFO)
            }
        }
    }

    private suspend fun getIdleEvent(): PeopleScreenEvent.Internal.Idle {
        delay(DELAY_BEFORE_RETURN_IDLE_EVENT)
        return PeopleScreenEvent.Internal.Idle
    }

    private suspend fun List<User>.toSortedList(filter: String = NO_FILTER): List<User> =
        withContext(defaultDispatcher) {
            val sortedList = ArrayList(this@toSortedList)
            sortedList.sortWith(compareBy<User> { it.presence }.thenBy { it.fullName })
            sortedList.filterByNameAndEmail(filter)
        }

    private fun List<User>.filterByNameAndEmail(filter: String): List<User> =
        if (filter.isBlank()) {
            this
        } else {
            val words = filter.splitToWords()
            filter { it.fullName.isContainingWords(words) || it.email.isContainingWords(words) }
        }

    private companion object {

        const val NO_FILTER = ""
        const val DELAY_BEFORE_SET_FILTER = 300L
        const val DELAY_BEFORE_CHECK_FILTER = 400L
        const val DELAY_BEFORE_UPDATE_INFO = 30_000L
        const val INDEX_NOT_FOUND = -1
        const val MILLIS_IN_SECOND = 1000
        const val OFFLINE_TIME = 180
        const val DELAY_BEFORE_RETURN_IDLE_EVENT = 1000L
    }
}