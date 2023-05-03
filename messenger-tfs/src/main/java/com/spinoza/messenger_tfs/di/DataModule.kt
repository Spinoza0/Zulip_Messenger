package com.spinoza.messenger_tfs.di

import android.content.Context
import androidx.room.Room
import com.spinoza.messenger_tfs.BuildConfig
import com.spinoza.messenger_tfs.data.database.MessengerDaoProvider
import com.spinoza.messenger_tfs.data.database.MessengerDaoProviderImpl
import com.spinoza.messenger_tfs.data.database.MessengerDatabase
import com.spinoza.messenger_tfs.data.network.ApiServiceProviderImpl
import com.spinoza.messenger_tfs.data.network.AppAuthKeeperImpl
import com.spinoza.messenger_tfs.data.network.AttachmentHandlerImpl
import com.spinoza.messenger_tfs.data.network.BaseUrlProviderImpl
import com.spinoza.messenger_tfs.data.network.WebUtilImpl
import com.spinoza.messenger_tfs.data.repository.WebRepositoryImpl
import com.spinoza.messenger_tfs.data.utils.createApiService
import com.spinoza.messenger_tfs.domain.network.ApiServiceProvider
import com.spinoza.messenger_tfs.domain.network.AppAuthKeeper
import com.spinoza.messenger_tfs.domain.network.AttachmentHandler
import com.spinoza.messenger_tfs.domain.network.BaseUrlProvider
import com.spinoza.messenger_tfs.domain.network.WebUtil
import com.spinoza.messenger_tfs.domain.repository.WebRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json

@Module
interface DataModule {

    @ApplicationScope
    @Binds
    fun bindWebRepository(impl: WebRepositoryImpl): WebRepository

    @ApplicationScope
    @Binds
    fun bindWebUtil(impl: WebUtilImpl): WebUtil

    @ApplicationScope
    @Binds
    fun bindAppAuthKeeper(impl: AppAuthKeeperImpl): AppAuthKeeper

    @ApplicationScope
    @Binds
    fun bindAttachmentHandler(impl: AttachmentHandlerImpl): AttachmentHandler

    companion object {

        @ApplicationScope
        @Provides
        fun provideApiServiceProvider(
            authKeeper: AppAuthKeeper,
            baseUrlProvider: BaseUrlProvider,
            json: Json,
        ): ApiServiceProvider {
            ApiServiceProviderImpl.value = baseUrlProvider.createApiService(authKeeper, json)
            return ApiServiceProviderImpl
        }

        @ApplicationScope
        @Provides
        fun provideBaseUrlProvider(): BaseUrlProvider = BaseUrlProviderImpl

        @ApplicationScope
        @Provides
        fun provideMessengerDatabase(context: Context): MessengerDatabase =
            Room.databaseBuilder(context, MessengerDatabase::class.java, BuildConfig.DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()

        @ApplicationScope
        @Provides
        fun provideMessengerDaoProvider(
            messengerDatabase: MessengerDatabase,
        ): MessengerDaoProvider = MessengerDaoProviderImpl.apply { value = messengerDatabase.dao() }

        @Provides
        fun provideJsonConverter(): Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
}