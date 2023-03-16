package com.spinoza.messenger_tfs.presentation.model

import com.spinoza.messenger_tfs.domain.repository.RepositoryState

sealed class MessagesFragmentState {

    class SendIconImage(val resId: Int) : MessagesFragmentState()

    class Repository(val state: RepositoryState) : MessagesFragmentState()
}