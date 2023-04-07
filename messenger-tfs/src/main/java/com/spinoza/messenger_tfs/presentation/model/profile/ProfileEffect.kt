package com.spinoza.messenger_tfs.presentation.model.profile

sealed class ProfileEffect {

    sealed class Failure : ProfileEffect() {

        class Network(val value: String) : Failure()

        class UserNotFound(val userId: String, val value: String) : Failure()
    }
}