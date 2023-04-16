package com.spinoza.messenger_tfs.presentation.feature.channels.model

sealed class ChannelsPageScreenEffect {

    sealed class Failure : ChannelsPageScreenEffect() {

        class Network(val value: String) : Failure()

        class Error(val value: String) : Failure()
    }
}