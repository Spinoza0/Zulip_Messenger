package com.spinoza.messenger_tfs.presentation.model.profile

sealed class ProfileEffect {

    sealed class Failure : ProfileEffect() {

        class ErrorNetwork(val value: String) : Failure()

        class ErrorUserLoading(val value: String) : Failure()
    }
}