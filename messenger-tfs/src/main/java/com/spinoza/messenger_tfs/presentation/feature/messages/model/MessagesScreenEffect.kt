package com.spinoza.messenger_tfs.presentation.feature.messages.model

sealed class MessagesScreenEffect {

    object MessageSent : MessagesScreenEffect()

    class ShowChooseReactionDialog(val messageId: Long) : MessagesScreenEffect()

    sealed class Failure : MessagesScreenEffect() {

        class ErrorNetwork(val value: String) : Failure()

        class ErrorMessages(val value: String) : Failure()
    }
}