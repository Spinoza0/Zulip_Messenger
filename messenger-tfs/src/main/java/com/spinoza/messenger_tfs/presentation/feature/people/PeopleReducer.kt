package com.spinoza.messenger_tfs.presentation.feature.people

import com.spinoza.messenger_tfs.presentation.feature.people.model.PeopleScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.people.model.PeopleScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.people.model.PeopleScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.people.model.PeopleScreenState
import com.spinoza.messenger_tfs.presentation.navigation.AppRouter
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import vivid.money.elmslie.core.store.dsl_reducer.ScreenDslReducer
import javax.inject.Inject

class PeopleReducer @Inject constructor(private val router: AppRouter) : ScreenDslReducer<
        PeopleScreenEvent,
        PeopleScreenEvent.Ui,
        PeopleScreenEvent.Internal,
        PeopleScreenState,
        PeopleScreenEffect,
        PeopleScreenCommand>(
    PeopleScreenEvent.Ui::class, PeopleScreenEvent.Internal::class
) {

    override fun Result.internal(event: PeopleScreenEvent.Internal) = when (event) {
        is PeopleScreenEvent.Internal.UsersLoaded -> {
            state { copy(isLoading = false, users = event.value) }
            commands { +PeopleScreenCommand.GetEvent }
        }

        is PeopleScreenEvent.Internal.EventFromQueue -> {
            state { copy(users = event.value) }
            commands { +PeopleScreenCommand.GetEvent }
        }

        is PeopleScreenEvent.Internal.EmptyQueueEvent -> commands { +PeopleScreenCommand.GetEvent }
        is PeopleScreenEvent.Internal.FilterChanged -> {
            if (state.users?.isEmpty() != false) {
                state { copy(isLoading = true) }
            }
            commands { +PeopleScreenCommand.GetFilteredList }
        }

        is PeopleScreenEvent.Internal.ErrorUserLoading -> {
            state { copy(isLoading = false) }
            effects { +PeopleScreenEffect.Failure.ErrorLoadingUsers(event.value) }
        }

        is PeopleScreenEvent.Internal.ErrorNetwork -> {
            state { copy(isLoading = false) }
            effects { +PeopleScreenEffect.Failure.ErrorNetwork(event.value) }
        }

        is PeopleScreenEvent.Internal.Idle -> {}
    }

    override fun Result.ui(event: PeopleScreenEvent.Ui) = when (event) {
        is PeopleScreenEvent.Ui.Load -> {
            state { copy(isLoading = true) }
            commands { +PeopleScreenCommand.Load }
        }

        is PeopleScreenEvent.Ui.OpenMainMenu -> router.navigateTo(Screens.MainMenu())
        is PeopleScreenEvent.Ui.ShowUserInfo -> router.navigateTo(Screens.UserProfile(event.userId))
        is PeopleScreenEvent.Ui.Filter -> commands { +PeopleScreenCommand.SetNewFilter(event.value) }
        is PeopleScreenEvent.Ui.OnScrolled -> {
            if (!event.canScrollUp && event.dy <= PeopleScreenEvent.DIRECTION_UP) {
                state { copy(isLoading = true) }
                commands { +PeopleScreenCommand.Load }
            } else if (!event.canScrollDown && event.dy >= PeopleScreenEvent.DIRECTION_DOWN) {
                state { copy(isLoading = true) }
                commands { +PeopleScreenCommand.Load }
            }
            effects {}
        }

        is PeopleScreenEvent.Ui.Init -> {}
    }
}