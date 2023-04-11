package org.kimp.tfs.hw7.presentation.people.elm

import vivid.money.elmslie.core.store.dsl_reducer.DslReducer

class Reducer : DslReducer<Event, State, Effect, Command>() {
    override fun Result.reduce(event: Event): Any = when (event) {
        is Event.Internal.UsersLoaded -> {
            state {
                copy(
                    loadedUsers = event.users,
                    error = null,
                    isLoading = false,
                    isEmptyState = false
                )
            }
        }

        is Event.Internal.LoadingError -> {
            state { copy(error = event.err, isLoading = false, isEmptyState = true) }
        }

        is Event.Ui.UsersListRequested -> {
            state { copy(isLoading = true, isEmptyState = false) }
            commands { +Command.LoadUsers }
        }

        is Event.Ui.FragmentInitialized -> {
            state { copy(isEmptyState = true) }
        }
    }
}
