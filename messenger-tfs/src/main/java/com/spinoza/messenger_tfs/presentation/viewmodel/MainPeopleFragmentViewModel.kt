package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult.Type
import com.spinoza.messenger_tfs.domain.usecase.GetAllUsersUseCase
import com.spinoza.messenger_tfs.presentation.model.ItemPeopleFragmentState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainPeopleFragmentViewModel(private val getAllUsersUseCase: GetAllUsersUseCase) :
    ViewModel() {

    val state: StateFlow<ItemPeopleFragmentState>
        get() = _state.asStateFlow()

    private val _state =
        MutableStateFlow<ItemPeopleFragmentState>(ItemPeopleFragmentState.Loading)

    fun loadAllUsers() {
        viewModelScope.launch {
            val result = getAllUsersUseCase()
            if (result.first.type == Type.SUCCESS) {
                _state.value = ItemPeopleFragmentState.Users(result.second)
            } else {
                _state.value = ItemPeopleFragmentState.Error(result.first)
            }
        }
    }
}