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
    private val getUserUseCase: GetUserUseCase?,
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
            val result = getUserUseCase?.invoke(userId)
            if (result != null) updateState(result)
        }
    }

    private fun updateState(result: Pair<RepositoryResult, User?>) {
        if (result.first.type == RepositoryResult.Type.SUCCESS) {
            result.second?.let { _state.value = ProfileScreenState.UserData(it) }
        } else {
            _state.value = ProfileScreenState.Error(result.first)
        }
    }
}