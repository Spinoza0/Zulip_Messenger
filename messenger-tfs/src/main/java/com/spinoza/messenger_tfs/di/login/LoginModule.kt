package com.spinoza.messenger_tfs.di.login

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.domain.usecase.GetApiKeyUseCase
import com.spinoza.messenger_tfs.presentation.feature.login.LoginActor
import com.spinoza.messenger_tfs.presentation.feature.login.LoginReducer
import com.spinoza.messenger_tfs.presentation.feature.login.LoginStorage
import com.spinoza.messenger_tfs.presentation.feature.login.LoginStorageImpl
import com.spinoza.messenger_tfs.presentation.feature.login.model.LoginScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.login.model.LoginScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.login.model.LoginScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.login.model.LoginScreenState
import dagger.Module
import dagger.Provides
import vivid.money.elmslie.coroutines.ElmStoreCompat

@Module
class LoginModule {

    @Provides
    fun provideLoginStorage(context: Context): LoginStorage = LoginStorageImpl(context)

    @Provides
    fun provideLoginScreenState(): LoginScreenState = LoginScreenState()

    @Provides
    fun provideLoginActor(lifecycle: Lifecycle, getApiKeyUseCase: GetApiKeyUseCase): LoginActor =
        LoginActor(lifecycle, getApiKeyUseCase)

    @Provides
    fun provideLoginReducer(
        loginStorage: LoginStorage,
        router: Router,
    ): LoginReducer = LoginReducer(loginStorage, router)

    @Provides
    fun provideLoginStore(
        state: LoginScreenState,
        actor: LoginActor,
        reducer: LoginReducer,
    ): ElmStoreCompat<
            LoginScreenEvent,
            LoginScreenState,
            LoginScreenEffect,
            LoginScreenCommand> =
        ElmStoreCompat(state, reducer, actor)
}