package com.spinoza.messenger_tfs.di

import android.content.Context
import androidx.room.Room
import com.spinoza.messenger_tfs.BuildConfig
import com.spinoza.messenger_tfs.data.database.MessengerDaoKeeper
import com.spinoza.messenger_tfs.data.database.MessengerDaoKeeperImpl
import com.spinoza.messenger_tfs.data.database.MessengerDatabase
import com.spinoza.messenger_tfs.data.network.apiservice.ApiServiceKeeperImpl
import com.spinoza.messenger_tfs.data.network.authorization.AppAuthKeeperImpl
import com.spinoza.messenger_tfs.data.network.attachment.AttachmentHandlerImpl
import com.spinoza.messenger_tfs.data.network.baseurl.BaseUrlKeeperImpl
import com.spinoza.messenger_tfs.data.network.WebUtilImpl
import com.spinoza.messenger_tfs.data.repository.DaoRepositoryImpl
import com.spinoza.messenger_tfs.data.network.ownuser.OwnUserKeeper
import com.spinoza.messenger_tfs.data.network.ownuser.OwnUserKeeperImpl
import com.spinoza.messenger_tfs.data.repository.WebRepositoryImpl
import com.spinoza.messenger_tfs.data.utils.createApiService
import com.spinoza.messenger_tfs.data.network.apiservice.ApiServiceKeeper
import com.spinoza.messenger_tfs.data.network.authorization.AppAuthKeeper
import com.spinoza.messenger_tfs.domain.network.AttachmentHandler
import com.spinoza.messenger_tfs.data.network.baseurl.BaseUrlKeeper
import com.spinoza.messenger_tfs.domain.network.WebUtil
import com.spinoza.messenger_tfs.domain.repository.DaoRepository
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
    fun bindDaoRepository(impl: DaoRepositoryImpl): DaoRepository

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
            baseUrlKeeper: BaseUrlKeeper,
            json: Json,
        ): ApiServiceKeeper {
            ApiServiceKeeperImpl.value = baseUrlKeeper.createApiService(authKeeper, json)
            return ApiServiceKeeperImpl
        }

        @ApplicationScope
        @Provides
        fun provideBaseUrlProvider(): BaseUrlKeeper = BaseUrlKeeperImpl

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
        ): MessengerDaoKeeper = MessengerDaoKeeperImpl.apply { value = messengerDatabase.dao() }

        @Provides
        fun provideJsonConverter(): Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        @Provides
        fun provideOwnUserKeeper(): OwnUserKeeper = OwnUserKeeperImpl
    }
}