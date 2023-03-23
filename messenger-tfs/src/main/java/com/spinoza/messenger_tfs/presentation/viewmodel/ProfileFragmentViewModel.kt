package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.usecase.GetCurrentUserUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetUserUseCase
import com.spinoza.messenger_tfs.presentation.state.ProfileScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileFragmentViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserUseCase: GetUserUseCase,
) : ViewModel() {

    val state: StateFlow<ProfileScreenState>
        get() = _state.asStateFlow()

    private val _state = MutableStateFlow<ProfileScreenState>(ProfileScreenState.Loading)

    fun loadCurrentUser() {
        viewModelScope.launch {
            updateState(getCurrentUserUseCase())
        }
    }

    fun loadUser(userId: Long) {
        viewModelScope.launch {
            updateState(getUserUseCase(userId))
        }
    }

    private fun updateState(result: RepositoryResult<User>) {
        when (result) {
            is RepositoryResult.Success -> _state.value = ProfileScreenState.UserData(result.value)
            is RepositoryResult.Failure.UserNotFound -> _state.value =
                ProfileScreenState.Failure.UserNotFound(result.userId)
            // TODO: process other errors
            else -> {}
        }
    }
}