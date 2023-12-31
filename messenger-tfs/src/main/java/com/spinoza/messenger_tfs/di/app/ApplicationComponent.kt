package com.spinoza.messenger_tfs.di.app

import android.content.Context
import com.spinoza.messenger_tfs.di.ApplicationScope
import com.spinoza.messenger_tfs.di.DispatcherDefault
import com.spinoza.messenger_tfs.di.DispatcherIO
import com.spinoza.messenger_tfs.domain.network.AttachmentHandler
import com.spinoza.messenger_tfs.domain.network.AuthorizationStorage
import com.spinoza.messenger_tfs.domain.network.WebLimitation
import com.spinoza.messenger_tfs.domain.network.WebUtil
import com.spinoza.messenger_tfs.domain.repository.ChannelRepository
import com.spinoza.messenger_tfs.domain.repository.DaoRepository
import com.spinoza.messenger_tfs.domain.repository.EventsRepository
import com.spinoza.messenger_tfs.domain.repository.MessageRepository
import com.spinoza.messenger_tfs.domain.repository.UserRepository
import com.spinoza.messenger_tfs.presentation.feature.app.MainActivity
import com.spinoza.messenger_tfs.presentation.navigation.AppRouter
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineDispatcher

@ApplicationScope
@Component(modules = [ApplicationModule::class, DataModule::class, StorageModule::class])
interface ApplicationComponent {

    fun inject(mainActivity: MainActivity)

    fun getContext(): Context

    fun getUserRepository(): UserRepository

    fun getMessageRepository(): MessageRepository

    fun getEventsRepository(): EventsRepository

    fun getChannelRepository(): ChannelRepository

    fun getDaoRepository(): DaoRepository

    fun getAttachmentHandler(): AttachmentHandler

    fun getAppRouter(): AppRouter

    fun getWebUtil(): WebUtil

    fun webLimitation(): WebLimitation

    fun getAuthorizationStorage(): AuthorizationStorage

    @DispatcherDefault
    fun getDispatcherDefault(): CoroutineDispatcher

    @DispatcherIO
    fun getDispatcherIO(): CoroutineDispatcher

    @Component.Factory
    interface Factory {

        fun create(@BindsInstance context: Context): ApplicationComponent
    }
}