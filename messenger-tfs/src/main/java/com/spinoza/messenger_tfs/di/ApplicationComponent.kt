package com.spinoza.messenger_tfs.di

import android.content.Context
import com.spinoza.messenger_tfs.domain.repository.MessengerRepository
import com.spinoza.messenger_tfs.domain.webutil.WebUtil
import com.spinoza.messenger_tfs.presentation.feature.app.MainActivity
import com.spinoza.messenger_tfs.presentation.navigation.AppRouter
import dagger.BindsInstance
import dagger.Component

@ApplicationScope
@Component(modules = [ApplicationModule::class, DataModule::class])
interface ApplicationComponent {

    fun inject(mainActivity: MainActivity)

    fun context(): Context

    fun messengerRepository(): MessengerRepository

    fun appRouter(): AppRouter

    fun webUtil(): WebUtil

    @Component.Factory
    interface Factory {

        fun create(@BindsInstance context: Context): ApplicationComponent
    }
}