package com.spinoza.messenger_tfs.di

import com.spinoza.messenger_tfs.data.network.ZulipApiFactory
import com.spinoza.messenger_tfs.data.network.ZulipApiService
import com.spinoza.messenger_tfs.data.network.ZulipAuthKeeper
import com.spinoza.messenger_tfs.data.repository.MessagesCache
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json

@Module
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

    @ApplicationScope
    @Provides
    fun provideMessagesRepository(
        messagesCache: MessagesCache,
        apiService: ZulipApiService,
        apiAuthKeeper: ZulipAuthKeeper,
        jsonConverter: Json,
    ): MessagesRepository =
        MessagesRepositoryImpl(messagesCache, apiService, apiAuthKeeper, jsonConverter)
}