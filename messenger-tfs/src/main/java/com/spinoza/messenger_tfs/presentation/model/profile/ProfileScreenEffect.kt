package com.spinoza.messenger_tfs.presentation.model.profile

sealed class ProfileScreenEffect {

    sealed class Failure : ProfileScreenEffect() {

        class ErrorNetwork(val value: String) : Failure()

        class ErrorUserLoading(val value: String) : Failure()
    }
}