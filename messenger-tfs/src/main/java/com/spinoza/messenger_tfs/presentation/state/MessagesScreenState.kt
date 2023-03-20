package com.spinoza.messenger_tfs.presentation.state

import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.presentation.model.MessagesResultDelegate

sealed class MessagesScreenState {

    object Loading : MessagesScreenState()

    class UpdateIconImage(val resId: Int) : MessagesScreenState()

    class Messages(val value: MessagesResultDelegate) : MessagesScreenState()

    class Error(val value: RepositoryResult) : MessagesScreenState()
}