package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.domain.repository.RepositoryState
import com.spinoza.messenger_tfs.domain.usecase.GetAllUsersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainPeopleFragmentViewModel(private val getAllUsersUseCase: GetAllUsersUseCase) :
    ViewModel() {

    val state: StateFlow<RepositoryState>
        get() = _state.asStateFlow()

    private val _state =
        MutableStateFlow<RepositoryState>(RepositoryState.Loading)

    fun getAllUsers() {
        viewModelScope.launch {
            _state.value = getAllUsersUseCase()
        }
    }
}