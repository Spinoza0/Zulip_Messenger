package org.kimp.tfs.hw7.presentation.streams.elm

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import org.kimp.tfs.hw7.data.api.Stream
import org.kimp.tfs.hw7.data.api.Topic

@Parcelize
data class State(
    val loadedChannels: @RawValue Map<Stream, List<Topic>>? = null,
    val error: Throwable? = null,
    val isLoading: Boolean = false,
    val isEmptyState: Boolean = false,
) : Parcelable


sealed class Event {
    sealed class Ui : Event() {
        object FragmentInitialized : Ui()
        data class ChannelsRequested(val subscribedOnly: Boolean) : Ui()
    }

    sealed class Internal : Event() {
        data class ChannelsLoaded(val channels: Map<Stream, List<Topic>>) : Internal()
        data class LoadingError(val err: Throwable) : Internal()
    }
}

sealed class Effect {}

sealed class Command {
    object LoadSubscribedChannels : Command()
    object LoadAllChannels : Command()
}
