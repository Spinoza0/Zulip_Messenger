package org.kimp.tfs.hw7.presentation.profile.elm

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.kimp.tfs.hw7.domain.ProfilesInteractor
import timber.log.Timber
import vivid.money.elmslie.coroutines.Actor
import javax.inject.Inject

class Actor @Inject constructor(
    private val profilesInteractor: ProfilesInteractor
): Actor<Command, Event> {
    override fun execute(command: Command): Flow<Event> = when (command) {
        is Command.LoadAuthenticatedUser -> profilesInteractor.getAuthenticatedUser()
        is Command.LoadSpecifiedUser -> flowOf(command.user)
    }.mapEvents(
        { data -> Event.Internal.UserLoaded(data) },
        { err -> Event.Internal.LoadingError(err) },
    )
}
