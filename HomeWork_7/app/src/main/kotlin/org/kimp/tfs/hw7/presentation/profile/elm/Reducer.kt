package org.kimp.tfs.hw7.presentation.profile.elm

import vivid.money.elmslie.core.store.dsl_reducer.DslReducer

class Reducer : DslReducer<Event, State, Effect, Command>() {
    override fun Result.reduce(event: Event): Any =
        when (event) {
            is Event.Internal.UserLoaded -> {
                state {
                    copy(
                        loadedProfile = event.user,
                        error = null,
                        isLoading = false,
                        isEmptyState = false
                    )
                }
            }

            is Event.Internal.LoadingError -> {
                state { copy(error = event.err, isLoading = false, isEmptyState = false) }
            }

            is Event.Ui.AuthenticatedUserRequested -> {
                state { copy(isLoading = true, isEmptyState = false) }
                commands { +Command.LoadAuthenticatedUser }
            }

            is Event.Ui.FragmentInitialized -> {
                state { copy(isEmptyState = true) }
            }
        }
}