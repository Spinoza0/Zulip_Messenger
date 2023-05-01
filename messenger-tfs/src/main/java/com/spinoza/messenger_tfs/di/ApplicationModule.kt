package com.spinoza.messenger_tfs.di

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.domain.notification.Notificator
import com.spinoza.messenger_tfs.presentation.feature.app.utils.NotificatorImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface ApplicationModule {

    @ApplicationScope
    @Binds
    fun bindNotificator(impl: NotificatorImpl): Notificator

    companion object {
        @ApplicationScope
        @Provides
        fun provideCicerone(): Cicerone<Router> = Cicerone.create()

        @ApplicationScope
        @Provides
        fun provideGlobalRouter(cicerone: Cicerone<Router>): Router = cicerone.router

        @ApplicationScope
        @Provides
        fun provideGlobalNavigatorHolder(cicerone: Cicerone<Router>): NavigatorHolder =
            cicerone.getNavigatorHolder()
    }
}