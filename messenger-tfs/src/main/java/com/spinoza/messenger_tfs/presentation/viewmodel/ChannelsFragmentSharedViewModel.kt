package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.presentation.model.SearchQuery
import com.spinoza.messenger_tfs.presentation.state.ChannelsScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChannelsFragmentSharedViewModel : ViewModel() {

    val state: StateFlow<ChannelsScreenState>
        get() = _state.asStateFlow()

    private val _state =
        MutableStateFlow<ChannelsScreenState>(ChannelsScreenState.Idle)

    private val searchQueryState = MutableSharedFlow<SearchQuery>()

    init {
        subscribeToSearchQueryChanges()
    }

    fun doOnTextChanged(screenPosition: Int, text: CharSequence?) {
        viewModelScope.launch {
            searchQueryState.emit(SearchQuery(screenPosition, text.toString()))
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
            .launchIn(viewModelScope)
    }

    private companion object {
        const val DURATION_MILLIS = 500L
    }
}