package com.spinoza.messenger_tfs.presentation.elm

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.cyberfox21.tinkofffintechseminar.di.GlobalDI
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.PresenceEvent
import com.spinoza.messenger_tfs.domain.repository.RepositoryError
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileCommand
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileEvent
import com.spinoza.messenger_tfs.presentation.utils.EventsQueueProcessor
import com.spinoza.messenger_tfs.presentation.utils.getErrorText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import vivid.money.elmslie.coroutines.Actor

class ProfileActor(lifecycle: Lifecycle) : Actor<ProfileCommand, ProfileEvent.Internal> {

    private val lifecycleScope = lifecycle.coroutineScope
    private val getOwnUserUseCase = GlobalDI.INSTANCE.getOwnUserUseCase
    private val getUserUseCase = GlobalDI.INSTANCE.getUserUseCase
    private val getPresenceEventsUseCase = GlobalDI.INSTANCE.getPresenceEventsUseCase

    private var eventsQueue = EventsQueueProcessor(lifecycleScope)
    private var user: User? = null
    private val actorFlow = MutableSharedFlow<ProfileEvent.Internal>()

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            eventsQueue.deleteQueue()
        }
    }

    init {
        lifecycle.addObserver(lifecycleObserver)
    }

    override fun execute(command: ProfileCommand): Flow<ProfileEvent.Internal> {
        when (command) {
            is ProfileCommand.LoadUser -> loadUser(command.userId)
            is ProfileCommand.LoadCurrentUser -> loadUser(CURRENT_USER)
            is ProfileCommand.SubscribePresence -> {
                user = command.user
                subscribePresence()
            }
        }
        return actorFlow.asSharedFlow()
    }

    private fun loadUser(userId: Long) {
        lifecycleScope.launch {
            val result =
                if (userId == CURRENT_USER) getOwnUserUseCase() else getUserUseCase(userId)
            result.onSuccess {
                user = it
                actorFlow.emit(ProfileEvent.Internal.UserLoaded(it))
                subscribePresence()
            }.onFailure {
                val event = if (it is RepositoryError) {
                    ProfileEvent.Internal.ErrorUserLoading(it.value)
                } else {
                    ProfileEvent.Internal.ErrorNetwork(it.getErrorText())
                }
                actorFlow.emit(event)
            }
        }
    }

    private fun subscribePresence() {
        lifecycleScope.launch {
            eventsQueue.registerQueue(EventType.PRESENCE, ::handleOnSuccessQueueRegistration)
        }
    }

    private fun handleOnSuccessQueueRegistration() {
        lifecycleScope.launch {
            while (true) {
                getPresenceEventsUseCase(eventsQueue.queue).onSuccess {
                    handlePresenceEvents(it)
                }
                delay(DELAY_BEFORE_UPDATE_INFO)
            }
        }
    }

    private fun handlePresenceEvents(presenceEvents: List<PresenceEvent>) {
        user?.let { userNotNull ->
            lifecycleScope.launch {
                presenceEvents.forEach { presenceEvent ->
                    eventsQueue.queue = eventsQueue.queue.copy(lastEventId = presenceEvent.id)
                    if (userNotNull.userId == presenceEvent.userId) {
                        val newUser = userNotNull.copy(presence = presenceEvent.presence)
                        user = newUser
                        actorFlow.emit(ProfileEvent.Internal.UserLoaded(newUser))
                    }
                }
            }
        }
    }

    private companion object {

        const val DELAY_BEFORE_UPDATE_INFO = 30_000L
        const val CURRENT_USER = -1L
    }
}