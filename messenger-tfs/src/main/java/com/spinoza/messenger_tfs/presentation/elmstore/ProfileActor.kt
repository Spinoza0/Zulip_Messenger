package com.spinoza.messenger_tfs.presentation.elmstore

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.spinoza.messenger_tfs.di.GlobalDI
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.repository.RepositoryError
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileScreenCommand
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileScreenEvent
import com.spinoza.messenger_tfs.presentation.utils.EventsQueueProcessor
import com.spinoza.messenger_tfs.presentation.utils.getErrorText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import vivid.money.elmslie.coroutines.Actor

class ProfileActor(lifecycle: Lifecycle) : Actor<ProfileScreenCommand, ProfileScreenEvent.Internal> {

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

    override fun execute(command: ProfileScreenCommand): Flow<ProfileScreenEvent.Internal> = flow {
        val event = when (command) {
            is ProfileScreenCommand.LoadUser -> loadUser(command.userId)
            is ProfileScreenCommand.LoadCurrentUser -> loadUser(CURRENT_USER)
            is ProfileScreenCommand.GetEvent -> if (isUserChanged) {
                isUserChanged = false
                user?.let { ProfileScreenEvent.Internal.UserLoaded(it) }
                    ?: ProfileScreenEvent.Internal.EmptyQueueEvent
            } else {
                delay(DELAY_BEFORE_UPDATE_INFO)
                ProfileScreenEvent.Internal.EmptyQueueEvent
            }
            is ProfileScreenCommand.SubscribePresence -> {
                changeUser(command.user)
                subscribePresence()
                ProfileScreenEvent.Internal.EmptyQueueEvent
            }
        }
        emit(event)
    }

    private suspend fun loadUser(userId: Long): ProfileScreenEvent.Internal {
        var event: ProfileScreenEvent.Internal = ProfileScreenEvent.Internal.Idle
        val result = if (userId == CURRENT_USER) getOwnUserUseCase() else getUserUseCase(userId)
        result.onSuccess {
            user = it
            event = ProfileScreenEvent.Internal.UserLoaded(it)
            subscribePresence()
        }.onFailure {
            event = if (it is RepositoryError) {
                ProfileScreenEvent.Internal.ErrorUserLoading(it.value)
            } else {
                ProfileScreenEvent.Internal.ErrorNetwork(it.getErrorText())
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