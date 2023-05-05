package com.spinoza.messenger_tfs.di.app

import android.content.Context
import androidx.room.Room
import com.spinoza.messenger_tfs.BuildConfig
import com.spinoza.messenger_tfs.data.database.MessengerDao
import com.spinoza.messenger_tfs.data.database.MessengerDatabase
import com.spinoza.messenger_tfs.data.network.AuthorizationStorageImpl
import com.spinoza.messenger_tfs.di.ApplicationScope
import com.spinoza.messenger_tfs.di.BaseUrl
import com.spinoza.messenger_tfs.domain.network.AuthorizationStorage
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface StorageModule {

    @ApplicationScope
    @Binds
    fun bindAuthorizationStorage(impl: AuthorizationStorageImpl): AuthorizationStorage

    companion object {

        @ApplicationScope
        @Provides
        @BaseUrl
        fun provideBaseUrl(): String = if (BuildConfig.ZULIP_SERVER_URL.endsWith(SLASH)) {
            BuildConfig.ZULIP_SERVER_URL.dropLast(SLASH.length)
        } else {
            BuildConfig.ZULIP_SERVER_URL
        }

        @ApplicationScope
        @Provides
        fun provideMessengerDao(context: Context): MessengerDao =
            Room.databaseBuilder(context, MessengerDatabase::class.java, BuildConfig.DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
                .dao()

        private const val SLASH = "/"
    }
}