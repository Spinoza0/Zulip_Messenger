package com.spinoza.messenger_tfs.presentation.model.channels

sealed class ChannelsPageEffect {

    sealed class Failure : ChannelsPageEffect() {

        class Network(val value: String) : Failure()

        class Error(val value: String) : Failure()
    }
}