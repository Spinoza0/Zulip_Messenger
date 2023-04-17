package com.spinoza.messenger_tfs.di.login

import com.spinoza.messenger_tfs.di.LoginScope
import com.spinoza.messenger_tfs.presentation.feature.login.LoginActor
import com.spinoza.messenger_tfs.presentation.feature.login.LoginReducer
import com.spinoza.messenger_tfs.presentation.feature.login.LoginStorage
import com.spinoza.messenger_tfs.presentation.feature.login.LoginStorageImpl
import com.spinoza.messenger_tfs.presentation.feature.login.model.LoginScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.login.model.LoginScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.login.model.LoginScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.login.model.LoginScreenState
import dagger.Binds
import dagger.Module
import dagger.Provides
import vivid.money.elmslie.coroutines.ElmStoreCompat

@Module(includes = [LoginModule.Bind::class])
class LoginModule {

    @Provides
    fun provideLoginScreenState(): LoginScreenState = LoginScreenState()

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

    @Module
    interface Bind {

        @LoginScope
        @Binds
        fun bindLoginStorage(impl: LoginStorageImpl): LoginStorage
    }
}