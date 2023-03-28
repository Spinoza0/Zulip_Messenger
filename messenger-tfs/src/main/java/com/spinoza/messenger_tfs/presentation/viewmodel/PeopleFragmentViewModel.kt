package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.usecase.GetUsersByFilterUseCase
import com.spinoza.messenger_tfs.presentation.state.PeopleScreenState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class PeopleFragmentViewModel(private val getUsersByFilterUseCase: GetUsersByFilterUseCase) :
    ViewModel() {

    val state: StateFlow<PeopleScreenState>
        get() = _state.asStateFlow()

    private var usersFilter = ""
    private val _state =
        MutableStateFlow<PeopleScreenState>(PeopleScreenState.Start)
    private val searchQueryState = MutableSharedFlow<String>()
    private var isFirstLoading = true

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
            when (val result = getUsersByFilterUseCase(usersFilter)) {
                is RepositoryResult.Success -> _state.emit(PeopleScreenState.Users(result.value))
                is RepositoryResult.Failure -> handleErrors(result)
            }
            setLoadingState.cancel()
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

    private companion object {

        const val DURATION_MILLIS = 300L
        const val DELAY_BEFORE_SET_STATE = 200L
    }
}