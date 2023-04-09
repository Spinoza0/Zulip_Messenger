package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.model.event.PresenceEvent
import com.spinoza.messenger_tfs.domain.repository.RepositoryError
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.getErrorText
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileEffect
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileEvent
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileState
import kotlinx.coroutines.Dispatchers
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
            _state.emit(_state.value.copy(isLoading = true))
            val result =
                if (userId == CURRENT_USER) getOwnUserUseCase() else getUserUseCase(userId)
            _state.emit(_state.value.copy(isLoading = false))
            result.onSuccess {
                _state.emit(state.value.copy(user = it))
                registerEventQueue()
            }.onFailure {
                val profileEffect = if (it is RepositoryError) {
                    ProfileEffect.Failure.UserNotFound(it.value)
                } else {
                    ProfileEffect.Failure.Network(it.getErrorText())
                }
                _effects.emit(profileEffect)
            }
        }
    }

    private fun registerEventQueue() {
        viewModelScope.launch {
            var isRegistrationSuccess = false
            while (!isRegistrationSuccess) {
                registerEventQueueUseCase(listOf(EventType.PRESENCE)).onSuccess {
                    eventsQueue = it
                    handleOnSuccessQueueRegistration()
                    isRegistrationSuccess = true
                }.onFailure {
                    delay(DELAY_BEFORE_REGISTRATION_ATTEMPT)
                }
            }
        }
    }

    private fun handleOnSuccessQueueRegistration() {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                getPresenceEventsUseCase(eventsQueue).onSuccess {
                    handlePresenceEvents(it)
                }
                delay(DELAY_BEFORE_UPDATE_INFO)
            }
        }
    }

    private fun handlePresenceEvents(presenceEvents: List<PresenceEvent>) {
        state.value.user?.let { user ->
            presenceEvents.forEach { presenceEvent ->
                eventsQueue = eventsQueue.copy(lastEventId = presenceEvent.id)
                if (user.userId == presenceEvent.userId) {
                    _state.value =
                        state.value.copy(user = user.copy(presence = presenceEvent.presence))
                }
            }
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
        const val DELAY_BEFORE_REGISTRATION_ATTEMPT = 10_000L
        const val CURRENT_USER = -1L
    }
}