package com.spinoza.messenger_tfs.di

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.presentation.feature.app.App
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule {

    @Provides
    fun provideGlobalRouter(): Router = App.router

    @Provides
    fun provideGlobalNavigatorHolder(): NavigatorHolder = App.navigatorHolder

    @Provides
    fun provideCicerone(): Cicerone<Router> = Cicerone.create()
}