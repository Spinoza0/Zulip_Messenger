package com.spinoza.messenger_tfs.presentation.feature.login

import androidx.lifecycle.LifecycleCoroutineScope
import com.spinoza.messenger_tfs.di.DispatcherDefault
import com.spinoza.messenger_tfs.domain.model.RepositoryError
import com.spinoza.messenger_tfs.domain.usecase.login.LogInUseCase
import com.spinoza.messenger_tfs.presentation.feature.login.model.LoginScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.login.model.LoginScreenEvent
import com.spinoza.messenger_tfs.presentation.util.getErrorText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import vivid.money.elmslie.coroutines.Actor
import javax.inject.Inject

class LoginActor @Inject constructor(
    private val lifecycleScope: LifecycleCoroutineScope,
    private val logInUseCase: LogInUseCase,
    @DispatcherDefault private val defaultDispatcher: CoroutineDispatcher,
) : Actor<LoginScreenCommand, LoginScreenEvent.Internal> {

    private val newEmailFieldState = MutableSharedFlow<String>()
    private val newPasswordFieldState = MutableSharedFlow<String>()
    private var isEmailValid = false
    private var isPasswordNotEmpty = false
    private var isEmailStatusChanged = false
    private var isPasswordStatusChanged = false

    init {
        subscribeToEmailFieldChanges()
        subscribeToPasswordFieldChanges()
    }

    override fun execute(command: LoginScreenCommand): Flow<LoginScreenEvent.Internal> =
        flow {
            val event = when (command) {
                is LoginScreenCommand.NewEmailText -> newEmailStatus(command.value)
                is LoginScreenCommand.NewPasswordText -> newPasswordStatus(command.value)
                is LoginScreenCommand.LogIn -> logIn(command)
            }
            emit(event)
        }

    private suspend fun newEmailStatus(text: CharSequence?): LoginScreenEvent.Internal {
        newEmailFieldState.emit(text.toString().trim())
        delay(DELAY_BEFORE_CHECK_FIELD_STATUS)
        if (isEmailStatusChanged) {
            isEmailStatusChanged = false
            return LoginScreenEvent.Internal.EmailStatus(isEmailValid)
        }
        return LoginScreenEvent.Internal.Idle
    }

    private suspend fun newPasswordStatus(text: CharSequence?): LoginScreenEvent.Internal {
        newPasswordFieldState.emit(text.toString().trim())
        delay(DELAY_BEFORE_CHECK_FIELD_STATUS)
        if (isPasswordStatusChanged) {
            isPasswordStatusChanged = false
            return LoginScreenEvent.Internal.PasswordStatus(isPasswordNotEmpty)
        }
        return LoginScreenEvent.Internal.Idle
    }

    private suspend fun logIn(command: LoginScreenCommand.LogIn): LoginScreenEvent.Internal {
        var event: LoginScreenEvent.Internal = LoginScreenEvent.Internal.Idle
        val email = command.email.trim()
        val password = command.password.trim()
        if (email.isNotBlank() && password.isNotBlank()) {
            logInUseCase(command.email.trim(), command.password.trim()).onSuccess {
                event = LoginScreenEvent.Internal.LoginSuccess
            }.onFailure { error ->
                event = if (error is RepositoryError) {
                    LoginScreenEvent.Internal.ErrorLogin(error.value)
                } else {
                    LoginScreenEvent.Internal.ErrorNetwork(error.getErrorText())
                }
            }
        }
        return event
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun subscribeToEmailFieldChanges() {
        newEmailFieldState
            .distinctUntilChanged()
            .debounce(DELAY_BEFORE_UPDATE_FIELD_STATUS)
            .flatMapLatest { flow { emit(it) } }
            .onEach {
                val oldStatus = isEmailValid
                isEmailValid =
                    it.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
                isEmailStatusChanged = oldStatus != isEmailValid
            }
            .flowOn(defaultDispatcher)
            .launchIn(lifecycleScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun subscribeToPasswordFieldChanges() {
        newPasswordFieldState
            .distinctUntilChanged()
            .debounce(DELAY_BEFORE_UPDATE_FIELD_STATUS)
            .flatMapLatest { flow { emit(it) } }
            .onEach {
                val oldStatus = isPasswordNotEmpty
                isPasswordNotEmpty = it.isNotEmpty()
                isPasswordStatusChanged = oldStatus != isPasswordNotEmpty
            }
            .flowOn(defaultDispatcher)
            .launchIn(lifecycleScope)
    }

    private companion object {

        const val DELAY_BEFORE_UPDATE_FIELD_STATUS = 200L
        const val DELAY_BEFORE_CHECK_FIELD_STATUS = 300L
    }
}