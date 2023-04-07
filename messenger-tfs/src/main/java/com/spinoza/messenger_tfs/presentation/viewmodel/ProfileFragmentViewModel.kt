package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.model.event.PresenceEvent
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.model.profilescreen.ProfileEffect
import com.spinoza.messenger_tfs.presentation.model.profilescreen.ProfileEvent
import com.spinoza.messenger_tfs.presentation.model.profilescreen.ProfileState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileFragmentViewModel(
    private val router: Router,
    private val getOwnUserUseCase: GetOwnUserUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val registerEventQueueUseCase: RegisterEventQueueUseCase,
    private val deleteEventQueueUseCase: DeleteEventQueueUseCase,
    private val getPresenceEventsUseCase: GetPresenceEventsUseCase,
) : ViewModel() {

    val state: StateFlow<ProfileState>
        get() = _state.asStateFlow()

    val effects: SharedFlow<ProfileEffect>
        get() = _effects.asSharedFlow()

    private val _state = MutableStateFlow(ProfileState())
    private val _effects = MutableSharedFlow<ProfileEffect>()
    private var eventsQueue = EventsQueue()

    fun reduce(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.Ui.LoadCurrentUser -> loadUser(CURRENT_USER)
            is ProfileEvent.Ui.LoadUser -> loadUser(event.userId)
            is ProfileEvent.Ui.GoBack -> router.exit()
        }
    }

    private fun loadUser(userId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result =
                if (userId == CURRENT_USER) getOwnUserUseCase() else getUserUseCase(userId)
            _state.value = _state.value.copy(isLoading = false)
            when (result) {
                is RepositoryResult.Success -> {
                    _state.value = state.value.copy(user = result.value)
                    registerEventQueue()
                }
                is RepositoryResult.Failure -> handleErrors(result)
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
                    handlePresenceEvents(eventResult.value)
                }
            }
        }
    }

    private fun handlePresenceEvents(presenceEvents: List<PresenceEvent>) {
        presenceEvents.forEach { presenceEvent ->
            eventsQueue = eventsQueue.copy(lastEventId = presenceEvent.id)
            state.value.user?.let { user ->
                if (user.userId == presenceEvent.userId) {
                    _state.value =
                        state.value.copy(user = user.copy(presence = presenceEvent.presence))
                }
            }
        }
    }

    private suspend fun handleErrors(error: RepositoryResult.Failure) {
        when (error) {
            is RepositoryResult.Failure.UserNotFound ->
                _effects.emit(ProfileEffect.Failure.UserNotFound("${error.userId}", error.value))
            is RepositoryResult.Failure.Network ->
                _effects.emit(ProfileEffect.Failure.Network(error.value))
            is RepositoryResult.Failure.RegisterEventQueue -> {}
            else -> {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            deleteEventQueueUseCase(eventsQueue.queueId)
        }
    }

    private companion object {

        const val DELAY_BEFORE_UPDATE_INFO = 30_000L
        const val CURRENT_USER = -1L
    }
}