package com.spinoza.messenger_tfs.presentation.elm

import androidx.lifecycle.LifecycleCoroutineScope
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.PresenceEvent
import com.spinoza.messenger_tfs.domain.repository.RepositoryError
import com.spinoza.messenger_tfs.domain.usecase.*
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
import vivid.money.elmslie.coroutines.ElmStoreCompat

class ProfileActor(
    private val lifecycleScope: LifecycleCoroutineScope,
    private val getOwnUserUseCase: GetOwnUserUseCase,
    private val getUserUseCase: GetUserUseCase,
    registerEventQueueUseCase: RegisterEventQueueUseCase,
    deleteEventQueueUseCase: DeleteEventQueueUseCase,
    private val getPresenceEventsUseCase: GetPresenceEventsUseCase,
) : Actor<ProfileCommand, ProfileEvent.Internal> {

    private var eventsQueue =
        EventsQueueProcessor(registerEventQueueUseCase, deleteEventQueueUseCase)
    private var user: User? = null
    private val actorFlow = MutableSharedFlow<ProfileEvent.Internal>()

    override fun execute(command: ProfileCommand): Flow<ProfileEvent.Internal> {
        when (command) {
            is ProfileCommand.LoadUser -> loadUser(command.userId)
            is ProfileCommand.LoadCurrentUser -> loadUser(CURRENT_USER)
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
                eventsQueue.registerQueue(EventType.PRESENCE, ::handleOnSuccessQueueRegistration)
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

class ProfileReducer(private val router: Router) :
    ScreenDslReducer<ProfileEvent, ProfileEvent.Ui, ProfileEvent.Internal, ProfileState, ProfileEffect, ProfileCommand>(
        ProfileEvent.Ui::class, ProfileEvent.Internal::class
    ) {

    override fun Result.internal(event: ProfileEvent.Internal) = when (event) {
        is ProfileEvent.Internal.UserLoaded ->
            state { copy(isLoading = false, user = event.user) }
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
        is ProfileEvent.Ui.Init -> {}
    }
}

fun profileStoreFactory(
    router: Router,
    coroutineScope: LifecycleCoroutineScope,
    getOwnUserUseCase: GetOwnUserUseCase,
    getUserUseCase: GetUserUseCase,
    registerEventQueueUseCase: RegisterEventQueueUseCase,
    deleteEventQueueUseCase: DeleteEventQueueUseCase,
    getPresenceEventsUseCase: GetPresenceEventsUseCase,
) = ElmStoreCompat(
    initialState = ProfileState(),
    reducer = ProfileReducer(router),
    actor = ProfileActor(
        coroutineScope,
        getOwnUserUseCase,
        getUserUseCase,
        registerEventQueueUseCase,
        deleteEventQueueUseCase,
        getPresenceEventsUseCase
    )
)

sealed class ProfileCommand {

    object LoadCurrentUser : ProfileCommand()

    class LoadUser(val userId: Long) : ProfileCommand()
}