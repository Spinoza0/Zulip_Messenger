package com.spinoza.messenger_tfs.presentation.model.login

sealed class LoginScreenEffect {

    class ButtonStatus(val isEnabled: Boolean) : LoginScreenEffect()

    sealed class Failure : LoginScreenEffect() {

        class ErrorNetwork(val value: String) : Failure()

        class ErrorLogin(val value: String) : Failure()
    }
}