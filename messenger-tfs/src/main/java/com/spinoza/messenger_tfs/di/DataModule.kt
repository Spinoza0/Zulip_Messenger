package com.spinoza.messenger_tfs.di

import com.spinoza.messenger_tfs.data.network.ZulipApiFactory
import com.spinoza.messenger_tfs.data.network.ZulipApiService
import com.spinoza.messenger_tfs.data.network.ZulipAuthKeeper
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json

@Module(includes = [DataModule.Bind::class])
class DataModule {

    @Provides
    fun provideJsonConverter(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @ApplicationScope
    @Provides
    fun provideZulipApiService(): ZulipApiService = ZulipApiFactory.apiService

    @ApplicationScope
    @Provides
    fun provideZulipAuthKeeper(): ZulipAuthKeeper = ZulipAuthKeeper

    @Module
    interface Bind {

        @ApplicationScope
        @Binds
        fun bindMessagesRepository(impl: MessagesRepositoryImpl): MessagesRepository
    }
}