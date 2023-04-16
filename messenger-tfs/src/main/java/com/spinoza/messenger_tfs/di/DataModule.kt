package com.spinoza.messenger_tfs.di

import com.spinoza.messenger_tfs.data.network.ZulipApiFactory
import com.spinoza.messenger_tfs.data.network.ZulipApiService
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

    @Provides
    fun provideZulipApiService(): ZulipApiService = ZulipApiFactory.apiService

    @Provides
    fun provideMessagesRepository(
        zulipApiService: ZulipApiService,
        jsonConverter: Json,
    ): MessagesRepository =
        MessagesRepositoryImpl.getInstance(zulipApiService, jsonConverter)
}