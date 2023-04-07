package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.usecase.DeleteEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetPresenceEventsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetUsersByFilterUseCase
import com.spinoza.messenger_tfs.domain.usecase.RegisterEventQueueUseCase
import com.spinoza.messenger_tfs.presentation.model.peoplescreen.PeopleEffect
import com.spinoza.messenger_tfs.presentation.model.peoplescreen.PeopleEvent
import com.spinoza.messenger_tfs.presentation.model.peoplescreen.PeopleState
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
            when (result) {
                is RepositoryResult.Success -> {
                    usersCache.clear()
                    usersCache.addAll(result.value)
                    _state.emit(state.value.copy(users = usersCache.toSortedList()))
                    registerEventQueue()
                }
                is RepositoryResult.Failure -> handleErrors(result)
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
            when (val queueResult = registerEventQueueUseCase(listOf(EventType.PRESENCE))) {
                is RepositoryResult.Success -> {
                    eventsQueue = queueResult.value
                    handleOnSuccessQueueRegistration()
                }
                is RepositoryResult.Failure -> handleErrors(queueResult)
            }
        }
    }

    private fun handleOnSuccessQueueRegistration() {
        viewModelScope.launch(Dispatchers.Default) {
            var lastUpdatingTimeStamp = 0L
            while (true) {
                delay(DELAY_BEFORE_UPDATE_INFO)
                val eventResult = getPresenceEventsUseCase(eventsQueue)
                if (eventResult is RepositoryResult.Success) {
                    var isListChanged = false
                    eventResult.value.forEach { event ->
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
                } else {
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
            }
        }
    }

    private suspend fun handleErrors(error: RepositoryResult.Failure) {
        when (error) {
            is RepositoryResult.Failure.LoadingUsers ->
                _effects.emit(PeopleEffect.Failure.LoadingUsers(error.value))
            is RepositoryResult.Failure.Network ->
                _effects.emit(PeopleEffect.Failure.Network(error.value))
            else -> {}
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
        const val INDEX_NOT_FOUND = -1
        const val MILLIS_IN_SECOND = 1000
        const val OFFLINE_TIME = 180
    }
}