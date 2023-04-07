package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.presentation.model.SearchQuery
import com.spinoza.messenger_tfs.presentation.model.channels.ChannelsEvent
import com.spinoza.messenger_tfs.presentation.model.channels.ChannelsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChannelsFragmentSharedViewModel : ViewModel() {

    val state: StateFlow<ChannelsState>
        get() = _state.asStateFlow()

    private val _state = MutableStateFlow(ChannelsState())
    private val searchQueryState = MutableSharedFlow<SearchQuery>()

    init {
        subscribeToSearchQueryChanges()
    }

    fun reduce(event: ChannelsEvent) {
        when (event) {
            is ChannelsEvent.Ui.Filter -> setFilter(event.value)
        }
    }

    private fun setFilter(searchQuery: SearchQuery) {
        viewModelScope.launch {
            searchQueryState.emit(searchQuery)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun subscribeToSearchQueryChanges() {
        searchQueryState
            .distinctUntilChanged()
            .debounce(DURATION_MILLIS)
            .flatMapLatest { flow { emit(it) } }
            .onEach { _state.value = state.value.copy(filter = it) }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    private companion object {
        const val DURATION_MILLIS = 300L
    }
}