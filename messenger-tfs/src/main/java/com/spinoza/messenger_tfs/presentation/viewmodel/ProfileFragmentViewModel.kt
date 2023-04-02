package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.usecase.*
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
    private val registerEventQueueUseCase: RegisterEventQueueUseCase,
    private val deletePresenceEventQueueUseCase: DeletePresenceEventQueueUseCase,
    private val getPresenceEventsUseCase: GetPresenceEventsUseCase,
) : ViewModel() {

    val state: StateFlow<ProfileScreenState>
        get() = _state.asStateFlow()

    private val _state =
        MutableStateFlow<ProfileScreenState>(ProfileScreenState.Idle)

    private lateinit var user: User
    private var eventsQueue = EventsQueue()

    fun loadCurrentUser() {
        loadUser(CURRENT_USER)
    }

    fun loadUser(userId: Long) {
        viewModelScope.launch {
            val setLoadingState = setLoadingStateWithDelay()
            val result =
                if (userId == CURRENT_USER) getOwnUserUseCase() else getUserUseCase(userId)
            setLoadingState.cancel()
            when (result) {
                is RepositoryResult.Success -> {
                    user = result.value
                    _state.value = ProfileScreenState.UserData(result.value)
                    registerEventQueue()
                }
                is RepositoryResult.Failure -> {
                    handleErrors(result)
                }
            }
        }
    }

    private fun registerEventQueue() {
        viewModelScope.launch {
            when (val queueResult = registerEventQueueUseCase(listOf(EventType.PRESENCE))) {
                is RepositoryResult.Success -> {
                    eventsQueue = queueResult.value
                    handleOnSuccessQueueRegistration()
                }
                is RepositoryResult.Failure -> handleErrors(queueResult)
            }
        }
    }

    private fun handleOnSuccessQueueRegistration() {
        viewModelScope.launch {
            while (true) {
                delay(DELAY_BEFORE_UPDATE_INFO)
                val eventResult = getPresenceEventsUseCase(eventsQueue)
                if (eventResult is RepositoryResult.Success) {
                    eventResult.value.forEach { event ->
                        eventsQueue.lastEventId = event.id
                        if (user.userId == event.userId) {
                            _state.emit(ProfileScreenState.Presence(event.presence))
                        }
                    }
                }
            }
        }
    }

    private fun handleErrors(error: RepositoryResult.Failure) {
        when (error) {
            is RepositoryResult.Failure.UserNotFound ->
                _state.value = ProfileScreenState.Failure.UserNotFound(error.userId, error.value)
            is RepositoryResult.Failure.Network ->
                _state.value = ProfileScreenState.Failure.Network(error.value)
            is RepositoryResult.Failure.RegisterPresenceEventQueue -> {}
            else -> {}
        }
    }

    private fun setLoadingStateWithDelay(): Job {
        return viewModelScope.launch {
            delay(DELAY_BEFORE_SHOW_SHIMMER)
            _state.value = ProfileScreenState.Loading
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            deletePresenceEventQueueUseCase(eventsQueue.queueId)
        }
    }

    private companion object {

        const val DELAY_BEFORE_SHOW_SHIMMER = 200L
        const val DELAY_BEFORE_UPDATE_INFO = 30_000L
        const val CURRENT_USER = -1L
    }
}