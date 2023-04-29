package com.spinoza.messenger_tfs.di

import com.spinoza.messenger_tfs.presentation.navigation.AppNavigatorHolder
import com.spinoza.messenger_tfs.presentation.navigation.AppNavigatorHolderImpl
import com.spinoza.messenger_tfs.presentation.navigation.AppRouter
import com.spinoza.messenger_tfs.presentation.navigation.AppRouterImpl
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
}