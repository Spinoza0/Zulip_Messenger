package com.spinoza.messenger_tfs.presentation.elmstore

import com.cyberfox21.tinkofffintechseminar.di.GlobalDI
import com.spinoza.messenger_tfs.presentation.model.people.PeopleEvent
import com.spinoza.messenger_tfs.presentation.model.people.PeopleScreenCommand
import com.spinoza.messenger_tfs.presentation.model.people.PeopleScreenEffect
import com.spinoza.messenger_tfs.presentation.model.people.PeopleScreenState
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import vivid.money.elmslie.core.store.dsl_reducer.ScreenDslReducer

class PeopleReducer : ScreenDslReducer<
        PeopleEvent,
        PeopleEvent.Ui,
        PeopleEvent.Internal,
        PeopleScreenState,
        PeopleScreenEffect,
        PeopleScreenCommand>(
    PeopleEvent.Ui::class, PeopleEvent.Internal::class
) {

    private val router = GlobalDI.INSTANCE.globalRouter

    override fun Result.internal(event: PeopleEvent.Internal) = when (event) {
        is PeopleEvent.Internal.UsersLoaded -> {
            state { copy(isLoading = false, users = event.value) }
            commands { +PeopleScreenCommand.GetEvent }
        }
        is PeopleEvent.Internal.EventFromQueue -> {
            state { copy(users = event.value) }
            commands { +PeopleScreenCommand.GetEvent }
        }
        is PeopleEvent.Internal.EmptyQueueEvent -> commands { +PeopleScreenCommand.GetEvent }
        is PeopleEvent.Internal.FilterChanged -> {
            if (state.users?.isEmpty() != false) {
                state { copy(isLoading = true) }
            }
            commands { +PeopleScreenCommand.GetFilteredList }
        }
        is PeopleEvent.Internal.ErrorUserLoading -> {
            state { copy(isLoading = false) }
            effects { +PeopleScreenEffect.Failure.ErrorLoadingUsers(event.value) }
        }
        is PeopleEvent.Internal.ErrorNetwork -> {
            state { copy(isLoading = false) }
            effects { +PeopleScreenEffect.Failure.ErrorNetwork(event.value) }
        }
        is PeopleEvent.Internal.Idle -> {}
    }

    override fun Result.ui(event: PeopleEvent.Ui) = when (event) {
        is PeopleEvent.Ui.Load -> {
            state { copy(isLoading = true) }
            commands { +PeopleScreenCommand.Load }
        }
        is PeopleEvent.Ui.OpenMainMenu -> router.navigateTo(Screens.MainMenu())
        is PeopleEvent.Ui.ShowUserInfo -> router.navigateTo(Screens.UserProfile(event.userId))
        is PeopleEvent.Ui.Filter -> commands { +PeopleScreenCommand.SetNewFilter(event.value) }
        is PeopleEvent.Ui.Init -> {}
    }
}