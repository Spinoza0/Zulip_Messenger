package com.spinoza.messenger_tfs.di

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.spinoza.messenger_tfs.BuildConfig
import com.spinoza.messenger_tfs.data.database.MessengerDao
import com.spinoza.messenger_tfs.data.database.MessengerDatabase
import com.spinoza.messenger_tfs.domain.model.AppAuthKeeper
import com.spinoza.messenger_tfs.data.network.ZulipApiService
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
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
    fun bindMessagesRepository(impl: MessagesRepositoryImpl): MessagesRepository

    companion object {

        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val MEDIA_TYPE_JSON = "application/json"
        private const val BASE_URL = "${BuildConfig.ZULIP_SERVER_URL}/api/v1/"
        private const val TIME_OUT_SECONDS = 15L

        @ApplicationScope
        @Provides
        fun provideMessengerDao(context: Context): MessengerDao =
            MessengerDatabase.getInstance(context).dao()

        @Provides
        fun provideJsonConverter(): Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        @ApplicationScope
        @Provides
        fun provideZulipApiService(appAuthKeeper: AppAuthKeeper): ZulipApiService {
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
                    if (request.header(HEADER_AUTHORIZATION) != null)
                        return@authenticator null
                    request.newBuilder().header(
                        HEADER_AUTHORIZATION,
                        appAuthKeeper.data
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