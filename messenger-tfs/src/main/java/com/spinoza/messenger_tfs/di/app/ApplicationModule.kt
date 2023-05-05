package com.spinoza.messenger_tfs.di.app

import com.spinoza.messenger_tfs.di.ApplicationScope
import com.spinoza.messenger_tfs.di.DispatcherDefault
import com.spinoza.messenger_tfs.di.DispatcherIO
import com.spinoza.messenger_tfs.presentation.navigation.AppNavigatorHolder
import com.spinoza.messenger_tfs.presentation.navigation.AppNavigatorHolderImpl
import com.spinoza.messenger_tfs.presentation.navigation.AppRouter
import com.spinoza.messenger_tfs.presentation.navigation.AppRouterImpl
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