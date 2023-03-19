package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.domain.repository.RepositoryState
import com.spinoza.messenger_tfs.domain.usecase.GetCurrentUserUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileFragmentViewModel(
    getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserUseCase: GetUserUseCase?,
) : ViewModel() {

    val user = getCurrentUserUseCase()

    val state: StateFlow<RepositoryState>
        get() = _state.asStateFlow()

    private val _state =
        MutableStateFlow<RepositoryState>(RepositoryState.Idle)

    fun getUserInfo(userId: Long) {
        viewModelScope.launch {
            getUserUseCase?.let {
                _state.value = getUserUseCase.invoke(userId)
            }
        }
    }
}