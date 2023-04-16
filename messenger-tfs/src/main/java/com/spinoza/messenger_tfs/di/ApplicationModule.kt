package com.spinoza.messenger_tfs.di

import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.presentation.feature.app.App
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule {

    @Provides
    fun provideGlobalRouter(): Router = App.router
}