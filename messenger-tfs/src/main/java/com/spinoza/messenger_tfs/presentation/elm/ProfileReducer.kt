package com.spinoza.messenger_tfs.presentation.elm

import com.cyberfox21.tinkofffintechseminar.di.GlobalDI
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileCommand
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileEffect
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileEvent
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileState
import vivid.money.elmslie.core.store.dsl_reducer.ScreenDslReducer

class ProfileReducer :
    ScreenDslReducer<ProfileEvent, ProfileEvent.Ui, ProfileEvent.Internal, ProfileState, ProfileEffect, ProfileCommand>(
        ProfileEvent.Ui::class, ProfileEvent.Internal::class
    ) {

    private val router = GlobalDI.INSTANCE.globalRouter

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