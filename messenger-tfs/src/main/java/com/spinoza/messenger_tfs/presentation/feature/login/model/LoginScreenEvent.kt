package com.spinoza.messenger_tfs.presentation.feature.login.model

sealed class LoginScreenEvent {

    sealed class Ui : LoginScreenEvent() {

        object Init : Ui()

        object Exit : Ui()

        class CheckLoginStatus(val logout: Boolean) : Ui()

        class ButtonPressed(val email: String, val password: String) : Ui()

        class NewEmailText(val value: CharSequence?) : Ui()

        class NewPasswordText(val value: CharSequence?) : Ui()
    }

    sealed class Internal : LoginScreenEvent() {

        object Idle : Internal()

        object LoginSuccess : Internal()

        class EmailStatus(val value: Boolean) : Internal()

        class PasswordStatus(val value: Boolean) : Internal()

        class ErrorNetwork(val value: String) : Internal()

        class ErrorLogin(val value: String) : Internal()
    }
}