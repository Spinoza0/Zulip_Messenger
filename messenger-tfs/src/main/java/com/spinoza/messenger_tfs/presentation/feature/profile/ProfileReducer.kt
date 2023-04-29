package com.spinoza.messenger_tfs.presentation.feature.profile

import com.spinoza.messenger_tfs.presentation.feature.profile.model.ProfileScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.profile.model.ProfileScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.profile.model.ProfileScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.profile.model.ProfileScreenState
import com.spinoza.messenger_tfs.presentation.navigation.AppRouter
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import vivid.money.elmslie.core.store.dsl_reducer.ScreenDslReducer
import javax.inject.Inject

class ProfileReducer @Inject constructor(private val router: AppRouter) : ScreenDslReducer<
        ProfileScreenEvent,
        ProfileScreenEvent.Ui,
        ProfileScreenEvent.Internal,
        ProfileScreenState,
        ProfileScreenEffect,
        ProfileScreenCommand>(
    ProfileScreenEvent.Ui::class, ProfileScreenEvent.Internal::class
) {

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

        is ProfileScreenEvent.Ui.Logout -> router.replaceScreen(Screens.Login(true))
        is ProfileScreenEvent.Ui.Init -> {}
    }
}