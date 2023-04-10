package com.spinoza.messenger_tfs.presentation.elm

import com.cyberfox21.tinkofffintechseminar.di.GlobalDI
import com.spinoza.messenger_tfs.presentation.model.people.PeopleCommand
import com.spinoza.messenger_tfs.presentation.model.people.PeopleEffect
import com.spinoza.messenger_tfs.presentation.model.people.PeopleEvent
import com.spinoza.messenger_tfs.presentation.model.people.PeopleState
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import vivid.money.elmslie.core.store.dsl_reducer.ScreenDslReducer

class PeopleReducer :
    ScreenDslReducer<PeopleEvent, PeopleEvent.Ui, PeopleEvent.Internal, PeopleState, PeopleEffect, PeopleCommand>(
        PeopleEvent.Ui::class, PeopleEvent.Internal::class
    ) {

    val router = GlobalDI.INSTANCE.globalRouter

    override fun Result.internal(event: PeopleEvent.Internal) = when (event) {
        is PeopleEvent.Internal.UsersLoaded ->
            state { copy(isLoading = false, users = event.value) }
        is PeopleEvent.Internal.PresencesLoaded ->
            state { copy(isLoading = false, users = event.value) }
        is PeopleEvent.Internal.ErrorUserLoading -> {
            state { copy(isLoading = false) }
            effects { +PeopleEffect.Failure.ErrorLoadingUsers(event.value) }
        }
        is PeopleEvent.Internal.ErrorNetwork -> {
            state { copy(isLoading = false) }
            effects { +PeopleEffect.Failure.ErrorNetwork(event.value) }
        }
        is PeopleEvent.Internal.Filter -> {
            state { copy(filter = event.value) }
        }
    }

    override fun Result.ui(event: PeopleEvent.Ui) = when (event) {
        is PeopleEvent.Ui.Init -> {
            state { copy(isLoading = true) }
            commands { +PeopleCommand.Load(state.filter) }
        }
        is PeopleEvent.Ui.Load -> {
            state { copy(isLoading = true) }
            commands { +PeopleCommand.Load(state.filter) }
        }
        is PeopleEvent.Ui.OpenMainMenu -> router.navigateTo(Screens.MainMenu())
        is PeopleEvent.Ui.ShowUserInfo -> router.navigateTo(Screens.UserProfile(event.userId))
        is PeopleEvent.Ui.Filter -> commands { +PeopleCommand.Filter(event.value) }
    }
}