package com.spinoza.messenger_tfs.di

import com.spinoza.messenger_tfs.domain.notification.Notificator
import com.spinoza.messenger_tfs.presentation.navigation.AppNavigatorHolder
import com.spinoza.messenger_tfs.presentation.navigation.AppNavigatorHolderImpl
import com.spinoza.messenger_tfs.presentation.navigation.AppRouter
import com.spinoza.messenger_tfs.presentation.navigation.AppRouterImpl
import com.spinoza.messenger_tfs.presentation.util.NotificatorImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
interface ApplicationModule {

    @ApplicationScope
    @Binds
    fun bindAppRouter(impl: AppRouterImpl): AppRouter

    @ApplicationScope
    @Binds
    fun bindAppNavigatorHolder(impl: AppNavigatorHolderImpl): AppNavigatorHolder

    @ApplicationScope
    @Binds
    fun bindNotificator(impl: NotificatorImpl): Notificator

    companion object {

        @ApplicationScope
        @Provides
        @DispatcherDefault
        fun provideDispatcherDefault(): CoroutineDispatcher = Dispatchers.Default

        @ApplicationScope
        @Provides
        @DispatcherIO
        fun provideDispatcherIO(): CoroutineDispatcher = Dispatchers.IO
    }
}