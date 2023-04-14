package com.spinoza.messenger_tfs.presentation.elmstore

import com.cyberfox21.tinkofffintechseminar.di.GlobalDI
import com.spinoza.messenger_tfs.presentation.model.login.LoginScreenCommand
import com.spinoza.messenger_tfs.presentation.model.login.LoginScreenEffect
import com.spinoza.messenger_tfs.presentation.model.login.LoginScreenEvent
import com.spinoza.messenger_tfs.presentation.model.login.LoginScreenState
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import vivid.money.elmslie.core.store.dsl_reducer.ScreenDslReducer

class LoginReducer : ScreenDslReducer<
        LoginScreenEvent,
        LoginScreenEvent.Ui,
        LoginScreenEvent.Internal,
        LoginScreenState,
        LoginScreenEffect,
        LoginScreenCommand>(
    LoginScreenEvent.Ui::class, LoginScreenEvent.Internal::class
) {

    private val router = GlobalDI.INSTANCE.globalRouter
    private var isEmailValid = false
    private var isPasswordNotEmpty = false

    override fun Result.internal(event: LoginScreenEvent.Internal) = when (event) {
        is LoginScreenEvent.Internal.EmailStatus -> {
            isEmailValid = event.value
            effects { +LoginScreenEffect.ButtonStatus(isEmailValid && isPasswordNotEmpty) }
        }
        is LoginScreenEvent.Internal.PasswordStatus -> {
            isPasswordNotEmpty = event.value
            effects { +LoginScreenEffect.ButtonStatus(isEmailValid && isPasswordNotEmpty) }
        }
        is LoginScreenEvent.Internal.LoginSuccess -> router.replaceScreen(Screens.MainMenu())
        is LoginScreenEvent.Internal.ErrorLogin -> {
            state { copy(isCheckingLogin = false) }
            effects { +LoginScreenEffect.Failure.ErrorLogin(event.value) }
        }
        is LoginScreenEvent.Internal.ErrorNetwork -> {
            state { copy(isCheckingLogin = false) }
            effects { +LoginScreenEffect.Failure.ErrorNetwork(event.value) }
        }
        is LoginScreenEvent.Internal.Idle -> {}
    }

    override fun Result.ui(event: LoginScreenEvent.Ui) = when (event) {
        is LoginScreenEvent.Ui.NewEmailText ->
            commands { +LoginScreenCommand.NewEmailText(event.value) }
        is LoginScreenEvent.Ui.NewPasswordText ->
            commands { +LoginScreenCommand.NewPasswordText(event.value) }
        is LoginScreenEvent.Ui.ButtonPressed -> {
            state { copy(isCheckingLogin = true) }
            commands { +LoginScreenCommand.ButtonPressed(event.email, event.password) }
        }
        is LoginScreenEvent.Ui.Exit -> router.exit()
        is LoginScreenEvent.Ui.Init -> {}
    }
}