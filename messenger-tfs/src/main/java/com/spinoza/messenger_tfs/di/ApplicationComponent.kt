package com.spinoza.messenger_tfs.di

import android.content.Context
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import dagger.BindsInstance
import dagger.Component

@Component(modules = [ApplicationModule::class, DataModule::class])
interface ApplicationComponent {

    fun context(): Context

    fun messagesRepository(): MessagesRepository

    fun globalRouter(): Router

    fun globalNavigatorHolder(): NavigatorHolder

    fun getCicerone(): Cicerone<Router>

    @Component.Factory
    interface Factory {

        fun create(@BindsInstance context: Context): ApplicationComponent
    }
}