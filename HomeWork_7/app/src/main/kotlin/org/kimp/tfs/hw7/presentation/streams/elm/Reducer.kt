package org.kimp.tfs.hw7.presentation.streams.elm

import vivid.money.elmslie.core.store.dsl_reducer.DslReducer

class Reducer : DslReducer<Event, State, Effect, Command>() {
    override fun Result.reduce(event: Event): Any =
        when (event) {
            is Event.Internal.ChannelsLoaded -> {
                state { copy(loadedChannels = event.channels, error = null, isLoading = false) }
            }
            is Event.Internal.LoadingError -> {
                state { copy(isLoading = false, loadedChannels = null, error = event.err, isEmptyState = false) }
            }
            is Event.Ui.ChannelsRequested -> {
                state { copy(isLoading = true, isEmptyState = false)}
                commands {
                    if (event.subscribedOnly) +Command.LoadSubscribedChannels
                    else +Command.LoadAllChannels
                }
            }
            is Event.Ui.FragmentInitialized -> {
                state { copy(isEmptyState = true) }
            }
        }
}