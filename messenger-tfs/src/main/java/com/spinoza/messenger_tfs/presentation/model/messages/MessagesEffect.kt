package com.spinoza.messenger_tfs.presentation.model.messages

sealed class MessagesEffect {

    object MessageSent : MessagesEffect()

    sealed class Failure : MessagesEffect() {

        class ErrorNetwork(val value: String) : Failure()

        class ErrorMessages(val value: String) : Failure()
    }
}