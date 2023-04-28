package com.spinoza.messenger_tfs.di

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import dagger.Module
import dagger.Provides

@Module
object ApplicationModule {

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