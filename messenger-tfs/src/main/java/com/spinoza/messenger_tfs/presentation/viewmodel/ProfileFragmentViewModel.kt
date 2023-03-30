package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.usecase.GetOwnUserUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetUserUseCase
import com.spinoza.messenger_tfs.presentation.state.ProfileScreenState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileFragmentViewModel(
    private val getOwnUserUseCase: GetOwnUserUseCase,
    private val getUserUseCase: GetUserUseCase,
) : ViewModel() {

    val state: StateFlow<ProfileScreenState>
        get() = _state.asStateFlow()

    private val _state =
        MutableStateFlow<ProfileScreenState>(ProfileScreenState.Idle)

    fun loadCurrentUser() {
        loadUser(CURRENT_USER)
    }

    fun loadUser(userId: Long) {
        viewModelScope.launch {
            var isFirstTime = true
            val setLoadingState = setLoadingStateWithDelay()
            while (true) {
                val result =
                    if (userId == CURRENT_USER) getOwnUserUseCase() else getUserUseCase(userId)
                if (isFirstTime) {
                    setLoadingState.cancel()
                    when (result) {
                        is RepositoryResult.Success ->
                            _state.value = ProfileScreenState.UserData(result.value)
                        is RepositoryResult.Failure -> {
                            handleErrors(result)
                            break
                        }
                    }
                } else if (result is RepositoryResult.Success) {
                    _state.value = ProfileScreenState.UserData(result.value)
                }
                delay(DELAY_BEFORE_UPDATE_INFO)
                isFirstTime = false
            }
        }
    }

    private fun handleErrors(error: RepositoryResult.Failure) {
        when (error) {
            is RepositoryResult.Failure.UserNotFound ->
                _state.value = ProfileScreenState.Failure.UserNotFound(error.userId, error.value)
            is RepositoryResult.Failure.Network ->
                _state.value = ProfileScreenState.Failure.Network(error.value)
            else -> {}
        }
    }

    private fun setLoadingStateWithDelay(): Job {
        return viewModelScope.launch {
            delay(DELAY_BEFORE_SET_STATE)
            _state.value = ProfileScreenState.Loading
        }
    }

    private companion object {

        const val DELAY_BEFORE_SET_STATE = 200L
        const val DELAY_BEFORE_UPDATE_INFO = 60000L
        const val CURRENT_USER = -1L
    }
}