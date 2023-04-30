package com.spinoza.messenger_tfs.di

import android.content.Context
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.spinoza.messenger_tfs.BuildConfig
import com.spinoza.messenger_tfs.data.database.MessengerDao
import com.spinoza.messenger_tfs.data.database.MessengerDatabase
import com.spinoza.messenger_tfs.data.network.AppAuthKeeperImpl
import com.spinoza.messenger_tfs.data.network.AttachmentHandlerImpl
import com.spinoza.messenger_tfs.data.network.AttachmentNotificatorImpl
import com.spinoza.messenger_tfs.data.network.WebUtilImpl
import com.spinoza.messenger_tfs.data.network.ZulipApiService
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.domain.authorization.AppAuthKeeper
import com.spinoza.messenger_tfs.domain.attachment.AttachmentHandler
import com.spinoza.messenger_tfs.domain.attachment.AttachmentNotificator
import com.spinoza.messenger_tfs.domain.repository.MessengerRepository
import com.spinoza.messenger_tfs.domain.util.WebUtil
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

@Module
interface DataModule {

    @ApplicationScope
    @Binds
    fun bindMessengerRepository(impl: MessagesRepositoryImpl): MessengerRepository

    @ApplicationScope
    @Binds
    fun bindWebUtil(impl: WebUtilImpl): WebUtil

    @ApplicationScope
    @Binds
    fun bindAppAuthKeeper(impl: AppAuthKeeperImpl): AppAuthKeeper

    @ApplicationScope
    @Binds
    fun bindAttachmentNotificator(impl: AttachmentNotificatorImpl): AttachmentNotificator

    @ApplicationScope
    @Binds
    fun bindAttachmentHandler(impl: AttachmentHandlerImpl): AttachmentHandler

    companion object {

        private const val DATABASE_NAME = "messenger-tfs-cache.db"
        private const val MEDIA_TYPE_JSON = "application/json"
        private const val BASE_URL = "${BuildConfig.ZULIP_SERVER_URL}/api/v1/"
        private const val TIME_OUT_SECONDS = 15L

        @ApplicationScope
        @Provides
        fun provideMessengerDatabase(context: Context): MessengerDatabase =
            Room.databaseBuilder(context, MessengerDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()

        @ApplicationScope
        @Provides
        fun provideMessengerDao(messengerDatabase: MessengerDatabase): MessengerDao =
            messengerDatabase.dao()

        @Provides
        fun provideJsonConverter(): Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        @ApplicationScope
        @Provides
        fun provideZulipApiService(authKeeper: AppAuthKeeper): ZulipApiService {
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
            val contentType = MediaType.get(MEDIA_TYPE_JSON)
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
                .authenticator { _: Route?, response: Response ->
                    val request = response.request()
                    if (request.header(authKeeper.getKey()) != null)
                        return@authenticator null
                    request.newBuilder().header(
                        authKeeper.getKey(),
                        authKeeper.getValue()
                    ).build()
                }
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(json.asConverterFactory(contentType))
                .client(okHttpClient)
                .build()
            return retrofit.create(ZulipApiService::class.java)
        }
    }
}