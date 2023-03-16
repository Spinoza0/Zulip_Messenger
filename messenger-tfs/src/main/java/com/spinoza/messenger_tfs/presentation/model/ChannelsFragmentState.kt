package com.spinoza.messenger_tfs.presentation.model

import com.spinoza.messenger_tfs.domain.repository.RepositoryState

sealed class ChannelsFragmentState {

    class Source(val type: SourceType) : ChannelsFragmentState()

    class Channels(val state: RepositoryState) : ChannelsFragmentState()

    enum class SourceType {
        SUBSCRIBED, ALL
    }
}