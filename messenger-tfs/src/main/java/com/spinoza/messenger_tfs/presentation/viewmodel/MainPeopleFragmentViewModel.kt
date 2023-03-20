package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult.Type
import com.spinoza.messenger_tfs.domain.usecase.GetAllUsersUseCase
import com.spinoza.messenger_tfs.presentation.state.PeopleScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainPeopleFragmentViewModel(private val getAllUsersUseCase: GetAllUsersUseCase) :
    ViewModel() {

    val state: StateFlow<PeopleScreenState>
        get() = _state.asStateFlow()

    private val _state = MutableStateFlow<PeopleScreenState>(PeopleScreenState.Loading)

    fun loadAllUsers() {
        viewModelScope.launch {
            val result = getAllUsersUseCase()
            if (result.first.type == Type.SUCCESS) {
                _state.value = PeopleScreenState.Users(result.second)
            } else {
                _state.value = PeopleScreenState.Error(result.first)
            }
        }
    }
}