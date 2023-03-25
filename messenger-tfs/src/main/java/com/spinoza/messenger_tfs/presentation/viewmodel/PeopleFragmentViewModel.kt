package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.usecase.GetUsersByFilterUseCase
import com.spinoza.messenger_tfs.presentation.state.PeopleScreenState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class PeopleFragmentViewModel(private val getUsersByFilterUseCase: GetUsersByFilterUseCase) :
    ViewModel() {

    val state: StateFlow<PeopleScreenState>
        get() = _state.asStateFlow()

    private val _state =
        MutableStateFlow<PeopleScreenState>(PeopleScreenState.Loading)
    private val searchQueryState = MutableSharedFlow<String>()
    private val useCasesScope = CoroutineScope(Dispatchers.IO)
    private var usersFilter = ""

    init {
        subscribeToSearchQueryChanges()
    }

    override fun onCleared() {
        super.onCleared()
        useCasesScope.cancel()
    }

    fun doOnTextChanged(searchQuery: CharSequence?) {
        useCasesScope.launch {
            searchQueryState.emit(searchQuery.toString())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun subscribeToSearchQueryChanges() {
        searchQueryState
            .distinctUntilChanged()
            .debounce(DURATION_MILLIS)
            .flatMapLatest { flow { emit(it) } }
            .onEach { _state.value = PeopleScreenState.Filter(it) }
            .flowOn(Dispatchers.Default)
            .launchIn(useCasesScope)
    }

    fun setUsersFilter(filter: String) {
        usersFilter = filter
    }

    fun loadUsers() {
        useCasesScope.launch {
            when (val result = getUsersByFilterUseCase(usersFilter)) {
                is RepositoryResult.Success -> _state.value = PeopleScreenState.Users(result.value)

                // TODO: process errors
                is RepositoryResult.Failure -> {}
            }
        }
    }

    private companion object {
        const val DURATION_MILLIS = 500L
    }
}