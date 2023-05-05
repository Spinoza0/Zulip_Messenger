package com.spinoza.messenger_tfs.di

import com.spinoza.messenger_tfs.BuildConfig
import com.spinoza.messenger_tfs.data.database.MessengerDao
import com.spinoza.messenger_tfs.domain.usermanager.UserManager
import com.spinoza.messenger_tfs.stub.MessengerDaoStub
import com.spinoza.messenger_tfs.stub.UserManagerStub
import dagger.Module
import dagger.Provides

@Module
object TestStorageModule {

    @ApplicationScope
    @Provides
    fun provideUserManager(): UserManager = UserManagerStub()

    @ApplicationScope
    @Provides
    @BaseUrl
    fun provideBaseUrl(): String = "http://localhost:${BuildConfig.MOCKWEBSERVER_PORT}"

    @ApplicationScope
    @Provides
    fun provideMessengerDao(): MessengerDao = MessengerDaoStub()
}