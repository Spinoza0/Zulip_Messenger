package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.presentation.model.channels.ChannelsScreenEvent
import com.spinoza.messenger_tfs.presentation.model.channels.ChannelsScreenState
import com.spinoza.messenger_tfs.presentation.model.channels.SearchQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChannelsFragmentSharedViewModel : ViewModel() {

    val state: StateFlow<ChannelsScreenState>
        get() = _state.asStateFlow()

    private val _state = MutableStateFlow(ChannelsScreenState())
    private val searchQueryState = MutableSharedFlow<SearchQuery>()

    init {
        subscribeToSearchQueryChanges()
    }

    fun accept(event: ChannelsScreenEvent) {
        when (event) {
            is ChannelsScreenEvent.Ui.Filter -> setFilter(event.value)
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
            .onEach { _state.emit(state.value.copy(filter = it)) }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    private companion object {
        const val DURATION_MILLIS = 300L
    }
}