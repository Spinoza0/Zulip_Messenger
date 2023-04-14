package com.spinoza.messenger_tfs.presentation.model.login

sealed class LoginScreenCommand {

    class NewEmailText(val value: CharSequence?) : LoginScreenCommand()

    class NewPasswordText(val value: CharSequence?) : LoginScreenCommand()

    class ButtonPressed(val email: String, val password: String) : LoginScreenCommand()
}