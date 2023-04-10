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
import com.spinoza.messenger_tfs.presentation.getErrorText
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileEffect
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileEvent
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileState
import com.spinoza.messenger_tfs.presentation.viewmodel.EventsQueueProcessor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import vivid.money.elmslie.core.store.dsl_reducer.ScreenDslReducer
import vivid.money.elmslie.coroutines.Actor

class ProfileActor(lifecycle: Lifecycle) : Actor<ProfileCommand, ProfileEvent.Internal> {

    private val lifecycleScope = lifecycle.coroutineScope
    private val getOwnUserUseCase = GlobalDI.INSTANCE.getOwnUserUseCase
    private val getUserUseCase = GlobalDI.INSTANCE.getUserUseCase
    private val registerEventQueueUseCase = GlobalDI.INSTANCE.registerEventQueueUseCase
    private val deleteEventQueueUseCase = GlobalDI.INSTANCE.deleteEventQueueUseCase
    private val getPresenceEventsUseCase = GlobalDI.INSTANCE.getPresenceEventsUseCase

    private var eventsQueue =
        EventsQueueProcessor(registerEventQueueUseCase, deleteEventQueueUseCase)
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

class ProfileReducer :
    ScreenDslReducer<ProfileEvent, ProfileEvent.Ui, ProfileEvent.Internal, ProfileState, ProfileEffect, ProfileCommand>(
        ProfileEvent.Ui::class, ProfileEvent.Internal::class
    ) {

    val router = GlobalDI.INSTANCE.globalRouter

    override fun Result.internal(event: ProfileEvent.Internal) = when (event) {
        is ProfileEvent.Internal.UserLoaded ->
            state { copy(isLoading = false, user = event.value) }
        is ProfileEvent.Internal.ErrorUserLoading -> {
            state { copy(isLoading = false) }
            effects { +ProfileEffect.Failure.ErrorUserLoading(event.value) }
        }
        is ProfileEvent.Internal.ErrorNetwork -> {
            state { copy(isLoading = false) }
            effects { +ProfileEffect.Failure.ErrorNetwork(event.value) }
        }
    }

    override fun Result.ui(event: ProfileEvent.Ui) = when (event) {
        is ProfileEvent.Ui.LoadCurrentUser -> {
            state { copy(isLoading = true) }
            commands { +ProfileCommand.LoadCurrentUser }
        }
        is ProfileEvent.Ui.LoadUser -> {
            state { copy(isLoading = true) }
            commands { +ProfileCommand.LoadUser(event.userId) }
        }
        is ProfileEvent.Ui.GoBack -> router.exit()
        is ProfileEvent.Ui.SubscribePresence -> event.user?.let {
            commands { +ProfileCommand.SubscribePresence(event.user) }
        }
        is ProfileEvent.Ui.Init -> {}
    }
}

sealed class ProfileCommand {

    object LoadCurrentUser : ProfileCommand()

    class SubscribePresence(val user: User) : ProfileCommand()

    class LoadUser(val userId: Long) : ProfileCommand()
}