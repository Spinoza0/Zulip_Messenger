package com.spinoza.messenger_tfs.presentation.feature.login.model

sealed class LoginScreenCommand {

    class NewEmailText(val value: CharSequence?) : LoginScreenCommand()

    class NewPasswordText(val value: CharSequence?) : LoginScreenCommand()

    class LogIn(val email: String, val password: String) : LoginScreenCommand()
}