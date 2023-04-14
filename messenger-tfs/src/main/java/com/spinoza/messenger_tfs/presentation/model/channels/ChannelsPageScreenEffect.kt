package com.spinoza.messenger_tfs.presentation.model.channels

sealed class ChannelsPageScreenEffect {

    sealed class Failure : ChannelsPageScreenEffect() {

        class Network(val value: String) : Failure()

        class Error(val value: String) : Failure()
    }
}