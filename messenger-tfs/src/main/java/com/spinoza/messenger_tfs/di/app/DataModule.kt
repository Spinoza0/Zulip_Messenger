package com.spinoza.messenger_tfs.di.app

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.spinoza.messenger_tfs.data.network.WebUtilImpl
import com.spinoza.messenger_tfs.data.network.apiservice.ZulipApiService
import com.spinoza.messenger_tfs.data.network.attachment.AttachmentHandlerImpl
import com.spinoza.messenger_tfs.data.network.authorization.AppAuthKeeper
import com.spinoza.messenger_tfs.data.network.authorization.AppAuthKeeperImpl
import com.spinoza.messenger_tfs.data.network.ownuser.OwnUserKeeper
import com.spinoza.messenger_tfs.data.network.ownuser.OwnUserKeeperImpl
import com.spinoza.messenger_tfs.data.repository.DaoRepositoryImpl
import com.spinoza.messenger_tfs.data.repository.WebRepositoryImpl
import com.spinoza.messenger_tfs.di.ApplicationScope
import com.spinoza.messenger_tfs.di.BaseUrl
import com.spinoza.messenger_tfs.domain.network.AttachmentHandler
import com.spinoza.messenger_tfs.domain.network.WebUtil
import com.spinoza.messenger_tfs.domain.repository.DaoRepository
import com.spinoza.messenger_tfs.domain.repository.WebRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Route
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

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
        fun provideApiService(
            authKeeper: AppAuthKeeper,
            jsonConverter: Json,
            @BaseUrl baseUrl: String,
        ): ZulipApiService {
            val contentType = MEDIA_TYPE_JSON.toMediaType()
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
                .authenticator { _: Route?, response: okhttp3.Response ->
                    if (response.request.header(authKeeper.getKey()) != null)
                        return@authenticator null
                    response.request.newBuilder().header(
                        authKeeper.getKey(),
                        authKeeper.getValue()
                    ).build()
                }
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl("$baseUrl/api/v1/")
                .addConverterFactory(jsonConverter.asConverterFactory(contentType))
                .client(okHttpClient)
                .build()
            return retrofit.create(ZulipApiService::class.java)
        }

        @Provides
        fun provideJsonConverter(): Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        @Provides
        fun provideOwnUserKeeper(): OwnUserKeeper = OwnUserKeeperImpl

        private const val MEDIA_TYPE_JSON = "application/json"
        private const val TIME_OUT_SECONDS = 15L
    }
}