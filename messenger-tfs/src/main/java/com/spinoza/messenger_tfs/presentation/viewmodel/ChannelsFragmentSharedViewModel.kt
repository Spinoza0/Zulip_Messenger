package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.spinoza.messenger_tfs.presentation.model.SearchQuery
import com.spinoza.messenger_tfs.presentation.state.ChannelsScreenState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class ChannelsFragmentSharedViewModel : ViewModel() {

    val state: StateFlow<ChannelsScreenState>
        get() = _state.asStateFlow()

    private val _state =
        MutableStateFlow<ChannelsScreenState>(ChannelsScreenState.Idle)
    private val searchQueryState = MutableSharedFlow<SearchQuery>()
    private val useCasesScope = CoroutineScope(Dispatchers.Default)

    init {
        subscribeToSearchQueryChanges()
    }

    override fun onCleared() {
        super.onCleared()
        useCasesScope.cancel()
    }

    fun doOnTextChanged(searchQuery: SearchQuery) {
        useCasesScope.launch {
            searchQueryState.emit(searchQuery)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun subscribeToSearchQueryChanges() {
        searchQueryState
            .distinctUntilChanged()
            .debounce(DURATION_MILLIS)
            .flatMapLatest { flow { emit(it) } }
            .onEach { _state.value = ChannelsScreenState.Filter(it) }
            .flowOn(Dispatchers.Default)
            .launchIn(useCasesScope)
    }

    private companion object {
        const val DURATION_MILLIS = 300L
    }
}