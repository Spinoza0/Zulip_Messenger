package com.spinoza.messenger_tfs.presentation.state

import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.MessagesResult
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

sealed class MessagesScreenState {

    object Loading : MessagesScreenState()

    class CurrentUser(val value: User) : MessagesScreenState()

    class UpdateIconImage(val resId: Int) : MessagesScreenState()

    class Messages(val value: MessagesResult) : MessagesScreenState()

    class Error(val value: RepositoryResult) : MessagesScreenState()
}