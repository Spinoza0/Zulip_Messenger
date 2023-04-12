package com.spinoza.messenger_tfs.presentation.elmstore

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.cyberfox21.tinkofffintechseminar.di.GlobalDI
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.repository.RepositoryError
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileCommand
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileEvent
import com.spinoza.messenger_tfs.presentation.utils.EventsQueueProcessor
import com.spinoza.messenger_tfs.presentation.utils.getErrorText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import vivid.money.elmslie.coroutines.Actor

class ProfileActor(lifecycle: Lifecycle) : Actor<ProfileCommand, ProfileEvent.Internal> {

    private val lifecycleScope = lifecycle.coroutineScope
    private val getOwnUserUseCase = GlobalDI.INSTANCE.getOwnUserUseCase
    private val getUserUseCase = GlobalDI.INSTANCE.getUserUseCase
    private val getPresenceEventsUseCase = GlobalDI.INSTANCE.getPresenceEventsUseCase

    private var eventsQueue = EventsQueueProcessor(lifecycleScope)
    private var user: User? = null
    private var isUserChanged = false

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            eventsQueue.deleteQueue()
        }
    }

    init {
        lifecycle.addObserver(lifecycleObserver)
    }

    override fun execute(command: ProfileCommand): Flow<ProfileEvent.Internal> = flow {
        val event = when (command) {
            is ProfileCommand.LoadUser -> loadUser(command.userId)
            is ProfileCommand.LoadCurrentUser -> loadUser(CURRENT_USER)
            is ProfileCommand.GetEvent -> if (isUserChanged) {
                isUserChanged = false
                user?.let { ProfileEvent.Internal.UserLoaded(it) }
                    ?: ProfileEvent.Internal.EmptyQueueEvent
            } else {
                delay(DELAY_BEFORE_UPDATE_INFO)
                ProfileEvent.Internal.EmptyQueueEvent
            }
            is ProfileCommand.SubscribePresence -> {
                changeUser(command.user)
                subscribePresence()
                ProfileEvent.Internal.EmptyQueueEvent
            }
        }
        emit(event)
    }

    private suspend fun loadUser(userId: Long): ProfileEvent.Internal {
        var event: ProfileEvent.Internal = ProfileEvent.Internal.Idle
        val result = if (userId == CURRENT_USER) getOwnUserUseCase() else getUserUseCase(userId)
        result.onSuccess {
            user = it
            event = ProfileEvent.Internal.UserLoaded(it)
            subscribePresence()
        }.onFailure {
            event = if (it is RepositoryError) {
                ProfileEvent.Internal.ErrorUserLoading(it.value)
            } else {
                ProfileEvent.Internal.ErrorNetwork(it.getErrorText())
            }
        }
        return event
    }

    private fun subscribePresence() {
        eventsQueue.registerQueue(EventType.PRESENCE, ::handleOnSuccessQueueRegistration)
    }

    private fun handleOnSuccessQueueRegistration() {
        lifecycleScope.launch(Dispatchers.Default) {
            while (user != null) {
                getPresenceEventsUseCase(eventsQueue.queue).onSuccess { events ->
                    user?.let { userNotNull ->
                        events.forEach { event ->
                            eventsQueue.queue = eventsQueue.queue.copy(lastEventId = event.id)
                            if (userNotNull.userId == event.userId) {
                                changeUser(userNotNull.copy(presence = event.presence))
                            }
                        }
                    }
                }
                delay(DELAY_BEFORE_UPDATE_INFO)
            }
        }
    }

    private fun changeUser(newUser: User) {
        user = newUser
        isUserChanged = true
    }

    private companion object {

        const val DELAY_BEFORE_UPDATE_INFO = 30_000L
        const val CURRENT_USER = -1L
    }
}