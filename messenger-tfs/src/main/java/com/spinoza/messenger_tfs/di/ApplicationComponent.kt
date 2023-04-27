package com.spinoza.messenger_tfs.di

import android.content.Context
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.domain.repository.AppAuthKeeper
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.webutil.WebUtil
import com.spinoza.messenger_tfs.presentation.feature.app.MainActivity
import dagger.BindsInstance
import dagger.Component

@ApplicationScope
@Component(modules = [ApplicationModule::class, DataModule::class])
interface ApplicationComponent {

    fun inject(mainActivity: MainActivity)

    fun context(): Context

    fun messagesRepository(): MessagesRepository

    fun router(): Router

    fun messagesRepositoryAuthKeeper(): AppAuthKeeper

    fun webUtil(): WebUtil

    @Component.Factory
    interface Factory {

        fun create(@BindsInstance context: Context): ApplicationComponent
    }
}