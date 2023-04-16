package com.spinoza.messenger_tfs.presentation.feature.login

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.spinoza.messenger_tfs.domain.repository.RepositoryError
import com.spinoza.messenger_tfs.domain.usecase.CheckLoginUseCase
import com.spinoza.messenger_tfs.presentation.feature.app.utils.getErrorText
import com.spinoza.messenger_tfs.presentation.feature.login.model.LoginScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.login.model.LoginScreenEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import vivid.money.elmslie.coroutines.Actor

class LoginActor(
    lifecycle: Lifecycle,
    private val checkLoginUseCase: CheckLoginUseCase,
) : Actor<LoginScreenCommand, LoginScreenEvent.Internal> {

    private val lifecycleScope = lifecycle.coroutineScope
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
                is LoginScreenCommand.ButtonPressed -> checkUserLogin(
                    command.apiKey,
                    command.email,
                    command.password
                )
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

    private suspend fun checkUserLogin(
        apiKey: String,
        email: String,
        password: String,
    ): LoginScreenEvent.Internal {
        var event: LoginScreenEvent.Internal = LoginScreenEvent.Internal.Idle
        checkLoginUseCase(apiKey, email.trim(), password.trim()).onSuccess { apiKeyResult ->
            event = LoginScreenEvent.Internal.LoginSuccess(apiKeyResult, email, password)
        }.onFailure { error ->
            event = if (error is RepositoryError) {
                LoginScreenEvent.Internal.ErrorLogin(error.value)
            } else {
                LoginScreenEvent.Internal.ErrorNetwork(error.getErrorText())
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
            .flowOn(Dispatchers.Default)
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
            .flowOn(Dispatchers.Default)
            .launchIn(lifecycleScope)
    }

    private companion object {

        const val DELAY_BEFORE_UPDATE_FIELD_STATUS = 200L
        const val DELAY_BEFORE_CHECK_FIELD_STATUS = 300L
    }
}