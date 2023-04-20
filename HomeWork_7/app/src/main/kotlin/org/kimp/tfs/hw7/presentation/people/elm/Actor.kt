package org.kimp.tfs.hw7.presentation.people.elm

import kotlinx.coroutines.flow.Flow
import org.kimp.tfs.hw7.domain.ProfilesInteractor
import vivid.money.elmslie.coroutines.Actor
import javax.inject.Inject


class Actor @Inject constructor(
    private val profilesInteractor: ProfilesInteractor
): Actor<Command, Event> {
    override fun execute(command: Command): Flow<Event> = when (command) {
        is Command.LoadUsers -> profilesInteractor.getAllUsers()
    }.mapEvents(
        { data -> Event.Internal.UsersLoaded(data) },
        { err -> Event.Internal.LoadingError(err) },
    )
}