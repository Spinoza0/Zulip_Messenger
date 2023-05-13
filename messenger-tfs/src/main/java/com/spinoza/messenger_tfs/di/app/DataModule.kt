package com.spinoza.messenger_tfs.di.app

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.spinoza.messenger_tfs.data.cache.MessagesCache
import com.spinoza.messenger_tfs.data.cache.MessagesCacheImpl
import com.spinoza.messenger_tfs.data.network.WebLimitationImpl
import com.spinoza.messenger_tfs.data.network.WebUtilImpl
import com.spinoza.messenger_tfs.data.network.apiservice.ZulipApiService
import com.spinoza.messenger_tfs.data.network.attachment.AttachmentHandlerImpl
import com.spinoza.messenger_tfs.data.repository.ChannelRepositoryImpl
import com.spinoza.messenger_tfs.data.repository.DaoRepositoryImpl
import com.spinoza.messenger_tfs.data.repository.EventsRepositoryImpl
import com.spinoza.messenger_tfs.data.repository.MessageRepositoryImpl
import com.spinoza.messenger_tfs.data.repository.UserRepositoryImpl
import com.spinoza.messenger_tfs.di.ApplicationScope
import com.spinoza.messenger_tfs.di.BaseUrl
import com.spinoza.messenger_tfs.domain.network.AttachmentHandler
import com.spinoza.messenger_tfs.domain.network.AuthorizationStorage
import com.spinoza.messenger_tfs.domain.network.WebLimitation
import com.spinoza.messenger_tfs.domain.network.WebUtil
import com.spinoza.messenger_tfs.domain.repository.ChannelRepository
import com.spinoza.messenger_tfs.domain.repository.DaoRepository
import com.spinoza.messenger_tfs.domain.repository.EventsRepository
import com.spinoza.messenger_tfs.domain.repository.MessageRepository
import com.spinoza.messenger_tfs.domain.repository.UserRepository
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
    fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @ApplicationScope
    @Binds
    fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository

    @ApplicationScope
    @Binds
    fun bindEventsRepository(impl: EventsRepositoryImpl): EventsRepository

    @ApplicationScope
    @Binds
    fun bindChannelRepository(impl: ChannelRepositoryImpl): ChannelRepository

    @ApplicationScope
    @Binds
    fun bindMessagesCache(impl: MessagesCacheImpl): MessagesCache

    @ApplicationScope
    @Binds
    fun bindDaoRepository(impl: DaoRepositoryImpl): DaoRepository

    @ApplicationScope
    @Binds
    fun bindWebUtil(impl: WebUtilImpl): WebUtil

    @ApplicationScope
    @Binds
    fun bindAttachmentHandler(impl: AttachmentHandlerImpl): AttachmentHandler

    @ApplicationScope
    @Binds
    fun bindWebLimitation(impl: WebLimitationImpl): WebLimitation

    companion object {

        @ApplicationScope
        @Provides
        fun provideApiService(
            authorizationStorage: AuthorizationStorage,
            jsonConverter: Json,
            @BaseUrl baseUrl: String,
        ): ZulipApiService {
            val contentType = MEDIA_TYPE_JSON.toMediaType()
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
                .authenticator { _: Route?, response: okhttp3.Response ->
                    if (response.request.header(authorizationStorage.getAuthHeaderTitle()) != null)
                        return@authenticator null
                    response.request.newBuilder().header(
                        authorizationStorage.getAuthHeaderTitle(),
                        authorizationStorage.getAuthHeaderValue()
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

        @ApplicationScope
        @Provides
        fun provideJsonConverter(): Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        private const val MEDIA_TYPE_JSON = "application/json"
        private const val TIME_OUT_SECONDS = 15L
    }
}