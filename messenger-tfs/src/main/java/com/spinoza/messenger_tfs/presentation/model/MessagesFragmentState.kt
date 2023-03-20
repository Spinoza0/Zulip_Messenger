package com.spinoza.messenger_tfs.presentation.model

import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.MessagesResult
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

sealed class MessagesFragmentState {

    object Loading : MessagesFragmentState()

    class CurrentUser(val value: User) : MessagesFragmentState()

    class UpdateIconImage(val resId: Int) : MessagesFragmentState()

    class Messages(val value: MessagesResult) : MessagesFragmentState()

    class Error(val value: RepositoryResult) : MessagesFragmentState()
}