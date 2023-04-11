package com.spinoza.messenger_tfs.presentation.model.messages

sealed class MessagesEffect {

    object MessageSent : MessagesEffect()

    class ShowChooseReactionDialog(val messageId: Long) : MessagesEffect()

    sealed class Failure : MessagesEffect() {

        class ErrorNetwork(val value: String) : Failure()

        class ErrorMessages(val value: String) : Failure()
    }
}