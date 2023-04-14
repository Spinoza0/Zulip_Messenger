package com.spinoza.messenger_tfs.presentation.elmstore

import com.spinoza.messenger_tfs.di.GlobalDI
import com.spinoza.messenger_tfs.presentation.model.login.LoginScreenCommand
import com.spinoza.messenger_tfs.presentation.model.login.LoginScreenEffect
import com.spinoza.messenger_tfs.presentation.model.login.LoginScreenEvent
import com.spinoza.messenger_tfs.presentation.model.login.LoginScreenState
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import com.spinoza.messenger_tfs.presentation.utils.LoginStorage
import vivid.money.elmslie.core.store.dsl_reducer.ScreenDslReducer

class LoginReducer(private val storage: LoginStorage) : ScreenDslReducer<
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
        is LoginScreenEvent.Internal.LoginSuccess -> {
            storage.saveData(event.email, event.password)
            router.replaceScreen(Screens.MainMenu())
        }
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
        is LoginScreenEvent.Ui.CheckPreviousLogin -> {
            if (event.logout) {
                storage.deleteData()
                state { copy(isNeedLogin = true) }
            } else {
                val password = storage.getPassword()
                val email = storage.getEmail()
                if (password.isNotEmpty() && email.isNotEmpty()) {
                    state { copy(isNeedLogin = false) }
                    commands { +LoginScreenCommand.ButtonPressed(email, password) }
                } else {
                    state { copy(isNeedLogin = true) }
                }
            }
        }
        is LoginScreenEvent.Ui.Exit -> router.exit()
        is LoginScreenEvent.Ui.Init -> {}
    }
}