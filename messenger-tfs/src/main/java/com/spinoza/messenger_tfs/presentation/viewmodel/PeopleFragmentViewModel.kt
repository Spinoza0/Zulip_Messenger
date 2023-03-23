package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.usecase.GetAllUsersUseCase
import com.spinoza.messenger_tfs.presentation.state.PeopleScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PeopleFragmentViewModel(private val getAllUsersUseCase: GetAllUsersUseCase) :
    ViewModel() {

    val state: StateFlow<PeopleScreenState>
        get() = _state.asStateFlow()

    private val _state =
        MutableStateFlow<PeopleScreenState>(PeopleScreenState.Loading)

    fun loadAllUsers() {
        viewModelScope.launch {
            when (val result = getAllUsersUseCase()) {
                is RepositoryResult.Success -> _state.value = PeopleScreenState.Users(result.value)

                // TODO: process errors
                is RepositoryResult.Failure -> {}
            }
        }
    }
}