package com.spinoza.messenger_tfs.presentation.state

import com.spinoza.messenger_tfs.presentation.model.MessagesResultDelegate

sealed class MessagesScreenState {

    object Loading : MessagesScreenState()

    class UpdateIconImage(val resId: Int) : MessagesScreenState()

    class Messages(val value: MessagesResultDelegate) : MessagesScreenState()

    sealed class Failure : MessagesScreenState() {

        class UserNotFound(val userId: Long) : Failure()

        class MessageNotFound(val messageId: Long) : Failure()
    }
}