package com.spinoza.messenger_tfs.di.app

import android.content.Context
import com.spinoza.messenger_tfs.di.ApplicationScope
import com.spinoza.messenger_tfs.di.DispatcherDefault
import com.spinoza.messenger_tfs.di.DispatcherIO
import com.spinoza.messenger_tfs.domain.network.AttachmentHandler
import com.spinoza.messenger_tfs.domain.network.WebUtil
import com.spinoza.messenger_tfs.domain.repository.DaoRepository
import com.spinoza.messenger_tfs.domain.repository.WebRepository
import com.spinoza.messenger_tfs.domain.usermanager.UserManager
import com.spinoza.messenger_tfs.presentation.feature.app.MainActivity
import com.spinoza.messenger_tfs.presentation.navigation.AppRouter
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineDispatcher

@ApplicationScope
@Component(modules = [ApplicationModule::class, DataModule::class, StorageModule::class])
interface ApplicationComponent {

    fun inject(mainActivity: MainActivity)

    fun context(): Context

    fun webRepository(): WebRepository

    fun daoRepository(): DaoRepository

    fun attachmentHandler(): AttachmentHandler

    fun appRouter(): AppRouter

    fun webUtil(): WebUtil

    fun userManager(): UserManager

    @DispatcherDefault
    fun dispatcherDefault(): CoroutineDispatcher

    @DispatcherIO
    fun dispatcherIO(): CoroutineDispatcher

    @Component.Factory
    interface Factory {

        fun create(@BindsInstance context: Context): ApplicationComponent
    }
}