package org.kimp.tfs.hw7.presentation.people.elm

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import org.kimp.tfs.hw7.data.api.Profile


@Parcelize
data class State(
    val loadedUsers: @RawValue List<Profile>? = null,
    val error: Throwable? = null,
    val isLoading: Boolean = false,
    val isEmptyState: Boolean = false,
) : Parcelable


sealed class Event {
    sealed class Ui : Event() {
        object FragmentInitialized : Ui()
        object UsersListRequested : Ui()
    }

    sealed class Internal : Event() {
        data class UsersLoaded(val users: List<Profile>) : Internal()
        data class LoadingError(val err: Throwable?) : Internal()
    }
}

sealed class Effect {}

sealed class Command {
    object LoadUsers : Command()
}
