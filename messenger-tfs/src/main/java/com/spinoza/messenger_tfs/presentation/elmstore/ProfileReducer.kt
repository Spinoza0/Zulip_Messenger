package com.spinoza.messenger_tfs.presentation.elmstore

import com.spinoza.messenger_tfs.di.GlobalDI
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileScreenCommand
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileScreenEffect
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileScreenEvent
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileScreenState
import vivid.money.elmslie.core.store.dsl_reducer.ScreenDslReducer

class ProfileReducer : ScreenDslReducer<
        ProfileScreenEvent,
        ProfileScreenEvent.Ui,
        ProfileScreenEvent.Internal,
        ProfileScreenState,
        ProfileScreenEffect,
        ProfileScreenCommand>(
    ProfileScreenEvent.Ui::class, ProfileScreenEvent.Internal::class
) {

    private val router = GlobalDI.INSTANCE.globalRouter

    override fun Result.internal(event: ProfileScreenEvent.Internal) = when (event) {
        is ProfileScreenEvent.Internal.UserLoaded -> {
            state { copy(isLoading = false, user = event.value) }
            commands { +ProfileScreenCommand.GetEvent }
        }
        is ProfileScreenEvent.Internal.EmptyQueueEvent -> commands { +ProfileScreenCommand.GetEvent }
        is ProfileScreenEvent.Internal.ErrorUserLoading -> {
            state { copy(isLoading = false) }
            effects { +ProfileScreenEffect.Failure.ErrorUserLoading(event.value) }
        }
        is ProfileScreenEvent.Internal.ErrorNetwork -> {
            state { copy(isLoading = false) }
            effects { +ProfileScreenEffect.Failure.ErrorNetwork(event.value) }
        }
        is ProfileScreenEvent.Internal.Idle -> {}
    }

    override fun Result.ui(event: ProfileScreenEvent.Ui) = when (event) {
        is ProfileScreenEvent.Ui.LoadCurrentUser -> {
            state { copy(isLoading = true) }
            commands { +ProfileScreenCommand.LoadCurrentUser }
        }
        is ProfileScreenEvent.Ui.LoadUser -> {
            state { copy(isLoading = true) }
            commands { +ProfileScreenCommand.LoadUser(event.userId) }
        }
        is ProfileScreenEvent.Ui.GoBack -> router.exit()
        is ProfileScreenEvent.Ui.SubscribePresence -> event.user?.let {
            commands { +ProfileScreenCommand.SubscribePresence(event.user) }
        }
        is ProfileScreenEvent.Ui.Init -> {}
    }
}