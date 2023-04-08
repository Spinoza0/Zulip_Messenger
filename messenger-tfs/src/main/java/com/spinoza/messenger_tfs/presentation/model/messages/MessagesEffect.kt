package com.spinoza.messenger_tfs.presentation.model.messages

import com.spinoza.messenger_tfs.domain.model.MessagesFilter

sealed class MessagesEffect {

    object MessageSent : MessagesEffect()

    sealed class Failure : MessagesEffect() {

        class Network(val value: String) : Failure()

        class OwnUserNotFound(val value: String) : Failure()

        class UserNotFound(val userId: Long, val value: String) : Failure()

        class MessageNotFound(val messageId: Long) : Failure()

        class LoadingMessages(val messagesFilter: MessagesFilter, val value: String) : Failure()

        class SendingMessage(val value: String) : Failure()

        class UpdatingReaction(val value: String) : Failure()
    }
}