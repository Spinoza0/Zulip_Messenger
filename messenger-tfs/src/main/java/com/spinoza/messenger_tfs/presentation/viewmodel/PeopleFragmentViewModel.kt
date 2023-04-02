package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.PresenceQueue
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.usecase.DeletePresenceEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetPresenceEventsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetUsersByFilterUseCase
import com.spinoza.messenger_tfs.domain.usecase.RegisterPresenceEventQueueUseCase
import com.spinoza.messenger_tfs.presentation.state.PeopleScreenState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class PeopleFragmentViewModel(
    private val getUsersByFilterUseCase: GetUsersByFilterUseCase,
    private val registerPresenceEventQueueUseCase: RegisterPresenceEventQueueUseCase,
    private val deletePresenceEventQueueUseCase: DeletePresenceEventQueueUseCase,
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
    private var presenceQueue = PresenceQueue()
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

    private fun loadUsers() {
        viewModelScope.launch {
            val setLoadingState = setLoadingStateWithDelay()
            val result = getUsersByFilterUseCase(usersFilter)
            setLoadingState.cancel()
            when (result) {
                is RepositoryResult.Success -> {
                    usersCache.clear()
                    usersCache.addAll(result.value)
                    _state.value = PeopleScreenState.Users(result.value)
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
            when (val queueResult = registerPresenceEventQueueUseCase()) {
                is RepositoryResult.Success -> {
                    presenceQueue = queueResult.value
                    handleOnSuccessQueueRegistration()
                }
                is RepositoryResult.Failure -> handleErrors(queueResult)
            }
        }
    }

    private fun handleOnSuccessQueueRegistration() {
        viewModelScope.launch {
            while (true) {
                delay(DELAY_BEFORE_UPDATE_INFO)
                val eventResult = getPresenceEventsUseCase(presenceQueue)
                if (eventResult is RepositoryResult.Success) {
                    var isListChanged = false
                    eventResult.value.forEach { event ->
                        presenceQueue.lastEventId = event.id
                        val index = usersCache.indexOfFirst { it.userId == event.userId }
                        if (index != INDEX_NOT_FOUND) {
                            usersCache[index] = usersCache[index].copy(presence = event.presence)
                            isListChanged = true
                        }
                    }
                    if (isListChanged) {
                        _state.emit(PeopleScreenState.Users(usersCache.toList()))
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

    private fun setLoadingStateWithDelay(): Job {
        return viewModelScope.launch {
            delay(DELAY_BEFORE_SET_STATE)
            _state.emit(PeopleScreenState.Loading)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            deletePresenceEventQueueUseCase(presenceQueue.queueId)
        }
    }

    private companion object {

        const val DURATION_MILLIS = 300L
        const val DELAY_BEFORE_SET_STATE = 200L
        const val DELAY_BEFORE_UPDATE_INFO = 30_000L
        const val INDEX_NOT_FOUND = -1
    }
}