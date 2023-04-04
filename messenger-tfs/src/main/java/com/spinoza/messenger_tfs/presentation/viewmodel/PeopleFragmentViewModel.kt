package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.usecase.DeleteEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetPresenceEventsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetUsersByFilterUseCase
import com.spinoza.messenger_tfs.domain.usecase.RegisterEventQueueUseCase
import com.spinoza.messenger_tfs.presentation.state.PeopleScreenState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

class PeopleFragmentViewModel(
    private val getUsersByFilterUseCase: GetUsersByFilterUseCase,
    private val registerEventQueueUseCase: RegisterEventQueueUseCase,
    private val deleteEventQueueUseCase: DeleteEventQueueUseCase,
    private val getPresenceEventsUseCase: GetPresenceEventsUseCase,
) :
    ViewModel() {

    val state: StateFlow<PeopleScreenState>
        get() = _state.asStateFlow()

    private var usersFilter = ""
    private val _state =
        MutableStateFlow<PeopleScreenState>(PeopleScreenState.Start)
    private val searchQueryState = MutableSharedFlow<String>()
    private var isFirstLoading = true
    private var eventsQueue = EventsQueue()
    private var usersCache = mutableListOf<User>()

    init {
        subscribeToSearchQueryChanges()
    }

    fun setUsersFilter(newFilter: String) {
        if (usersFilter != newFilter) {
            usersFilter = newFilter
            loadUsers()
        } else if (isFirstLoading) {
            isFirstLoading = false
            loadUsers()
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            val setLoadingState = setLoadingStateWithDelay()
            val result = getUsersByFilterUseCase(usersFilter)
            setLoadingState.cancel()
            when (result) {
                is RepositoryResult.Success -> {
                    usersCache.clear()
                    usersCache.addAll(result.value)
                    _state.value = PeopleScreenState.Users(usersCache.toSortedList())
                    registerEventQueue()
                }
                is RepositoryResult.Failure -> handleErrors(result)
            }
        }
    }

    fun doOnTextChanged(searchQuery: CharSequence?) {
        viewModelScope.launch {
            searchQueryState.emit(searchQuery.toString())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun subscribeToSearchQueryChanges() {
        searchQueryState
            .distinctUntilChanged()
            .debounce(DURATION_MILLIS)
            .flatMapLatest { flow { emit(it) } }
            .onEach { setUsersFilter(it) }
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
        viewModelScope.launch {
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
                        _state.emit(PeopleScreenState.Users(usersCache.toSortedList()))
                    }
                } else {
                    val currentTimeStamp = System.currentTimeMillis() / MILLIS_IN_SECOND
                    if (currentTimeStamp - lastUpdatingTimeStamp > OFFLINE_TIME) {
                        for (index in 0 until usersCache.size) {
                            usersCache[index] =
                                usersCache[index].copy(presence = User.Presence.OFFLINE)
                        }
                        lastUpdatingTimeStamp = currentTimeStamp
                        _state.emit(PeopleScreenState.Users(usersCache.toSortedList()))
                    }
                }
            }
        }
    }

    private suspend fun handleErrors(error: RepositoryResult.Failure) {
        when (error) {
            is RepositoryResult.Failure.LoadingUsers -> {
                _state.emit(PeopleScreenState.Failure.LoadingUsers(error.value))
            }
            else -> {}
        }
    }

    private fun List<User>.toSortedList(): List<User> {
        val sortedList = ArrayList(this)
        sortedList.sortWith(compareBy<User> { it.presence }.thenBy { it.fullName })
        return sortedList
    }

    private fun setLoadingStateWithDelay(): Job {
        return viewModelScope.launch {
            delay(DELAY_BEFORE_SHOW_SHIMMER)
            _state.emit(PeopleScreenState.Loading)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            deleteEventQueueUseCase(eventsQueue.queueId)
        }
    }

    private companion object {

        const val DURATION_MILLIS = 300L
        const val DELAY_BEFORE_SHOW_SHIMMER = 200L
        const val DELAY_BEFORE_UPDATE_INFO = 30_000L
        const val INDEX_NOT_FOUND = -1
        const val MILLIS_IN_SECOND = 1000
        const val OFFLINE_TIME = 180
    }
}