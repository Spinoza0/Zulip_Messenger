package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.repository.RepositoryError
import com.spinoza.messenger_tfs.domain.usecase.DeleteEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetPresenceEventsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetUsersByFilterUseCase
import com.spinoza.messenger_tfs.domain.usecase.RegisterEventQueueUseCase
import com.spinoza.messenger_tfs.presentation.getErrorText
import com.spinoza.messenger_tfs.presentation.model.people.PeopleEffect
import com.spinoza.messenger_tfs.presentation.model.people.PeopleEvent
import com.spinoza.messenger_tfs.presentation.model.people.PeopleState
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class PeopleFragmentViewModel(
    private val router: Router,
    private val getUsersByFilterUseCase: GetUsersByFilterUseCase,
    private val registerEventQueueUseCase: RegisterEventQueueUseCase,
    private val deleteEventQueueUseCase: DeleteEventQueueUseCase,
    private val getPresenceEventsUseCase: GetPresenceEventsUseCase,
) :
    ViewModel() {

    val state: StateFlow<PeopleState>
        get() = _state.asStateFlow()
    val effects: SharedFlow<PeopleEffect>
        get() = _effects.asSharedFlow()

    private val _state = MutableStateFlow(PeopleState())
    private val _effects = MutableSharedFlow<PeopleEffect>()
    private val searchQueryState = MutableSharedFlow<String>()
    private var eventsQueue = EventsQueue()
    private var usersCache = mutableListOf<User>()

    init {
        subscribeToSearchQueryChanges()
        setFilter(state.value.filter)
        loadUsers(state.value.filter)
    }

    fun reduce(event: PeopleEvent) {
        when (event) {
            is PeopleEvent.Ui.Filter -> setFilter(event.value)
            is PeopleEvent.Ui.Load -> loadUsers(state.value.filter)
            is PeopleEvent.Ui.OpenMainMenu -> router.navigateTo(Screens.MainMenu())
            is PeopleEvent.Ui.ShowUserInfo -> router.navigateTo(Screens.UserProfile(event.userId))
        }
    }

    private fun loadUsers(filter: String) {
        viewModelScope.launch(Dispatchers.Default) {
            _state.emit(state.value.copy(isLoading = true, filter = filter))
            val result = getUsersByFilterUseCase(filter)
            _state.emit(state.value.copy(isLoading = false))
            result.onSuccess {
                usersCache.clear()
                usersCache.addAll(it)
                _state.emit(state.value.copy(users = usersCache.toSortedList()))
                registerEventQueue()
            }.onFailure { error ->
                val peopleEffect = if (error is RepositoryError) {
                    PeopleEffect.Failure.LoadingUsers(error.value)
                } else {
                    PeopleEffect.Failure.Network(error.getErrorText())
                }
                _effects.emit(peopleEffect)
            }
        }
    }

    private fun setFilter(filter: String) {
        viewModelScope.launch {
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
            .launchIn(viewModelScope)
    }

    private fun registerEventQueue() {
        viewModelScope.launch {
            var isRegistrationSuccess = false
            while (!isRegistrationSuccess) {
                registerEventQueueUseCase(listOf(EventType.PRESENCE)).onSuccess {
                    eventsQueue = it
                    handleOnSuccessQueueRegistration()
                    isRegistrationSuccess = true
                }.onFailure {
                    delay(DELAY_BEFORE_REGISTRATION_ATTEMPT)
                }
            }
        }
    }

    private fun handleOnSuccessQueueRegistration() {
        viewModelScope.launch(Dispatchers.Default) {
            var lastUpdatingTimeStamp = 0L
            while (true) {
                getPresenceEventsUseCase(eventsQueue).onSuccess { events ->
                    var isListChanged = false
                    events.forEach { event ->
                        eventsQueue = eventsQueue.copy(lastEventId = event.id)
                        val index = usersCache.indexOfFirst { it.userId == event.userId }
                        if (index != INDEX_NOT_FOUND) {
                            usersCache[index] = usersCache[index].copy(presence = event.presence)
                            isListChanged = true
                        }
                    }
                    if (isListChanged) {
                        lastUpdatingTimeStamp = System.currentTimeMillis() / MILLIS_IN_SECOND
                        _state.emit(state.value.copy(users = usersCache.toSortedList()))
                    }
                }.onFailure {
                    val currentTimeStamp = System.currentTimeMillis() / MILLIS_IN_SECOND
                    if (currentTimeStamp - lastUpdatingTimeStamp > OFFLINE_TIME) {
                        for (index in 0 until usersCache.size) {
                            usersCache[index] =
                                usersCache[index].copy(presence = User.Presence.OFFLINE)
                        }
                        lastUpdatingTimeStamp = currentTimeStamp
                        _state.emit(state.value.copy(users = usersCache.toSortedList()))
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

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            deleteEventQueueUseCase(eventsQueue.queueId)
        }
    }

    private companion object {

        const val DURATION_MILLIS = 300L
        const val DELAY_BEFORE_UPDATE_INFO = 30_000L
        const val DELAY_BEFORE_REGISTRATION_ATTEMPT = 10_000L
        const val INDEX_NOT_FOUND = -1
        const val MILLIS_IN_SECOND = 1000
        const val OFFLINE_TIME = 180
    }
}