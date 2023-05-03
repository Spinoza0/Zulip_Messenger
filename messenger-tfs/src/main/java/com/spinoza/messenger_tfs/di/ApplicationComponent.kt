package com.spinoza.messenger_tfs.di

import android.content.Context
import com.spinoza.messenger_tfs.domain.network.AttachmentHandler
import com.spinoza.messenger_tfs.domain.network.WebUtil
import com.spinoza.messenger_tfs.domain.notification.Notificator
import com.spinoza.messenger_tfs.domain.repository.DaoRepository
import com.spinoza.messenger_tfs.domain.repository.WebRepository
import com.spinoza.messenger_tfs.presentation.feature.app.MainActivity
import com.spinoza.messenger_tfs.presentation.navigation.AppRouter
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineDispatcher

@ApplicationScope
@Component(modules = [ApplicationModule::class, DataModule::class])
interface ApplicationComponent {

    fun inject(mainActivity: MainActivity)

    fun context(): Context

    fun webRepository(): WebRepository

    fun daoRepository(): DaoRepository

    fun attachmentHandler(): AttachmentHandler

    fun appRouter(): AppRouter

    fun webUtil(): WebUtil

    fun notificator(): Notificator

    @DispatcherDefault
    fun dispatcherDefault(): CoroutineDispatcher

    @DispatcherIO
    fun dispatcherIO(): CoroutineDispatcher

    @Component.Factory
    interface Factory {

        fun create(@BindsInstance context: Context): ApplicationComponent
    }
}