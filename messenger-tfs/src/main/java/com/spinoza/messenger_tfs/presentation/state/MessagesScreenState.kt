package com.spinoza.messenger_tfs.presentation.state

import com.spinoza.messenger_tfs.presentation.model.MessagesResultDelegate

sealed class MessagesScreenState {

    object Loading : MessagesScreenState()

    object MessageSent : MessagesScreenState()

    object ReactionSent : MessagesScreenState()

    class UpdateIconImage(val resId: Int) : MessagesScreenState()

    class Messages(val value: MessagesResultDelegate) : MessagesScreenState()

    sealed class Failure : MessagesScreenState() {

        class Network(val value: String) : Failure()

        class OwnUserNotFound(val value: String) : Failure()

        class UserNotFound(val userId: Long) : Failure()

        class MessageNotFound(val messageId: Long) : Failure()

        class SendingMessage(val value: String) : Failure()

        class UpdatingReaction(val value: String) : Failure()
    }
}