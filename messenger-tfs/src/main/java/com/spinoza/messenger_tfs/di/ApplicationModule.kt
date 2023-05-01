package com.spinoza.messenger_tfs.di

import com.spinoza.messenger_tfs.domain.notification.Notificator
import com.spinoza.messenger_tfs.presentation.navigation.AppNavigatorHolder
import com.spinoza.messenger_tfs.presentation.navigation.AppNavigatorHolderImpl
import com.spinoza.messenger_tfs.presentation.navigation.AppRouter
import com.spinoza.messenger_tfs.presentation.navigation.AppRouterImpl
import com.spinoza.messenger_tfs.presentation.util.NotificatorImpl
import dagger.Binds
import dagger.Module

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
}