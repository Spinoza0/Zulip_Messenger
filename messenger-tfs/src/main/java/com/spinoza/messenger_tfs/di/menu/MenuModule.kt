package com.spinoza.messenger_tfs.di.menu

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.Router
import dagger.Module
import dagger.Provides

@Module
class MenuModule {

    @Provides
    fun provideCicerone(): Cicerone<Router> = Cicerone.create()
}