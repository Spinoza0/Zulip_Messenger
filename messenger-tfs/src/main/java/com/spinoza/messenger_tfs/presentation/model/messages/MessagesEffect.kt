package com.spinoza.messenger_tfs.presentation.model.messages

sealed class MessagesEffect {

    object MessageSent : MessagesEffect()

    sealed class Failure : MessagesEffect() {

        class Network(val value: String) : Failure()

        class Error(val value: String) : Failure()
    }
}