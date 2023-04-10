package com.spinoza.messenger_tfs.presentation.elm

import androidx.lifecycle.LifecycleCoroutineScope
import com.cyberfox21.tinkofffintechseminar.di.GlobalDI
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.repository.RepositoryError
import com.spinoza.messenger_tfs.presentation.getErrorText
import com.spinoza.messenger_tfs.presentation.model.people.PeopleEffect
import com.spinoza.messenger_tfs.presentation.model.people.PeopleEvent
import com.spinoza.messenger_tfs.presentation.model.people.PeopleState
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import com.spinoza.messenger_tfs.presentation.viewmodel.EventsQueueProcessor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import vivid.money.elmslie.core.store.dsl_reducer.ScreenDslReducer
import vivid.money.elmslie.coroutines.Actor

class PeopleActor(
    private val lifecycleScope: LifecycleCoroutineScope,
) : Actor<PeopleCommand, PeopleEvent.Internal> {

    private val getUsersByFilterUseCase = GlobalDI.INSTANCE.getUsersByFilterUseCase
    private val registerEventQueueUseCase = GlobalDI.INSTANCE.registerEventQueueUseCase
    private val deleteEventQueueUseCase = GlobalDI.INSTANCE.deleteEventQueueUseCase
    private val getPresenceEventsUseCase = GlobalDI.INSTANCE.getPresenceEventsUseCase

    private var eventsQueue =
        EventsQueueProcessor(registerEventQueueUseCase, deleteEventQueueUseCase)
    private val actorFlow = MutableSharedFlow<PeopleEvent.Internal>()
    private val searchQueryState = MutableSharedFlow<String>()
    private var usersCache = mutableListOf<User>()

    override fun execute(command: PeopleCommand): Flow<PeopleEvent.Internal> {
        when (command) {
            is PeopleCommand.Init -> {
                subscribeToSearchQueryChanges()
                setFilter(command.filter)
            }
            is PeopleCommand.Load -> loadUsers(command.filter)
            is PeopleCommand.SubscribePresence -> subscribePresence()
            is PeopleCommand.Filter -> setFilter(command.filter)
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
            .onEach { actorFlow.emit(PeopleEvent.Internal.Filter(it)) }
            .flowOn(Dispatchers.Default)
            .launchIn(lifecycleScope)
    }

    private fun loadUsers(filter: String) {
        lifecycleScope.launch(Dispatchers.Default) {
            getUsersByFilterUseCase(filter).onSuccess {
                usersCache.clear()
                usersCache.addAll(it)
                actorFlow.emit(PeopleEvent.Internal.UsersLoaded(usersCache.toSortedList()))
                subscribePresence()
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

    private fun subscribePresence() {
        lifecycleScope.launch {
            eventsQueue.registerQueue(EventType.PRESENCE, ::handleOnSuccessQueueRegistration)
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
                        actorFlow.emit(PeopleEvent.Internal.UsersLoaded(usersCache.toSortedList()))
                    }
                }.onFailure {
                    val currentTimeStamp = System.currentTimeMillis() / MILLIS_IN_SECOND
                    if (currentTimeStamp - lastUpdatingTimeStamp > OFFLINE_TIME) {
                        for (index in 0 until usersCache.size) {
                            usersCache[index] =
                                usersCache[index].copy(presence = User.Presence.OFFLINE)
                        }
                        lastUpdatingTimeStamp = currentTimeStamp
                        actorFlow.emit(PeopleEvent.Internal.UsersLoaded(usersCache.toSortedList()))
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

class PeopleReducer :
    ScreenDslReducer<PeopleEvent, PeopleEvent.Ui, PeopleEvent.Internal, PeopleState, PeopleEffect, PeopleCommand>(
        PeopleEvent.Ui::class, PeopleEvent.Internal::class
    ) {

    val router = GlobalDI.INSTANCE.globalRouter

    override fun Result.internal(event: PeopleEvent.Internal) = when (event) {
        is PeopleEvent.Internal.UsersLoaded ->
            state { copy(isLoading = false, users = event.value) }
        is PeopleEvent.Internal.ErrorUserLoading -> {
            state { copy(isLoading = false) }
            effects { +PeopleEffect.Failure.ErrorLoadingUsers(event.value) }
        }
        is PeopleEvent.Internal.ErrorNetwork -> {
            state { copy(isLoading = false) }
            effects { +PeopleEffect.Failure.ErrorNetwork(event.value) }
        }
        is PeopleEvent.Internal.Filter -> {
            state { copy(isLoading = true, filter = event.value) }
            commands { +PeopleCommand.Load(event.value) }
        }
    }

    override fun Result.ui(event: PeopleEvent.Ui) = when (event) {
        is PeopleEvent.Ui.Init -> {
            state { copy(isLoading = true) }
            commands { +PeopleCommand.Init(state.filter) }
        }
        is PeopleEvent.Ui.Load -> {
            state { copy(isLoading = true) }
            commands { +PeopleCommand.Load(state.filter) }
        }
        is PeopleEvent.Ui.OpenMainMenu -> router.navigateTo(Screens.MainMenu())
        is PeopleEvent.Ui.ShowUserInfo -> router.navigateTo(Screens.UserProfile(event.userId))
        is PeopleEvent.Ui.Filter -> commands { +PeopleCommand.Filter(event.value) }
    }
}

sealed class PeopleCommand {

    class Init(val filter: String) : PeopleCommand()

    class Load(val filter: String) : PeopleCommand()

    class Filter(val filter: String) : PeopleCommand()

    class SubscribePresence(val user: User) : PeopleCommand()
}