package org.kimp.tfs.hw7.presentation.streams.elm

import kotlinx.coroutines.flow.Flow
import org.kimp.tfs.hw7.domain.ChannelsInteractor
import vivid.money.elmslie.coroutines.Actor
import javax.inject.Inject

class Actor @Inject constructor (
    private val channelsInteractor: ChannelsInteractor
): Actor<Command, Event> {
    override fun execute(command: Command): Flow<Event> = when (command) {
        is Command.LoadAllChannels -> channelsInteractor.getChannels(false)
        is Command.LoadSubscribedChannels -> channelsInteractor.getChannels(true)
    }.mapEvents(
        { data -> Event.Internal.ChannelsLoaded(data) },
        { err -> Event.Internal.LoadingError(err) },
    )
}