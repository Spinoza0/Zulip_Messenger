package com.spinoza.messenger_tfs.presentation.feature.profile.model

sealed class ProfileScreenEffect {

    sealed class Failure : ProfileScreenEffect() {

        class ErrorNetwork(val value: String) : Failure()

        class ErrorUserLoading(val value: String) : Failure()
    }
}