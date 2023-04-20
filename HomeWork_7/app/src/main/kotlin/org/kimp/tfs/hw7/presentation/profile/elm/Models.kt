package org.kimp.tfs.hw7.presentation.profile.elm

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import org.kimp.tfs.hw7.data.api.Profile

@Parcelize
data class State(
    val loadedProfile: @RawValue Profile? = null,
    val error: Throwable? = null,
    val isLoading: Boolean = false,
    val isEmptyState: Boolean = false
) : Parcelable


sealed class Event {
    sealed class Ui : Event() {
        object FragmentInitialized : Ui()
        object AuthenticatedUserRequested : Ui()
    }

    sealed class Internal : Event() {
        data class UserLoaded(val user: Profile) : Internal()
        data class LoadingError(val err: Throwable) : Internal()
    }
}

sealed class Effect {}

sealed class Command {
    object LoadAuthenticatedUser : Command()
    data class LoadSpecifiedUser(val user: Profile) : Command()
}
