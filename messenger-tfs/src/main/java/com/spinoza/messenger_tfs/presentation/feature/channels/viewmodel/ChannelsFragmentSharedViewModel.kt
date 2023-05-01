package com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.di.DispatcherDefault
import com.spinoza.messenger_tfs.presentation.feature.channels.model.ChannelsScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.channels.model.ChannelsScreenState
import com.spinoza.messenger_tfs.presentation.feature.channels.model.SearchQuery
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChannelsFragmentSharedViewModel @Inject constructor(
    @DispatcherDefault private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

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
            .debounce(DELAY_BEFORE_FILTER_CHANGE)
            .flatMapLatest { flow { emit(it) } }
            .onEach { _state.emit(state.value.copy(filter = it)) }
            .flowOn(defaultDispatcher)
            .launchIn(viewModelScope)
    }

    companion object {
        const val DELAY_BEFORE_FILTER_CHANGE = 300L
    }
}