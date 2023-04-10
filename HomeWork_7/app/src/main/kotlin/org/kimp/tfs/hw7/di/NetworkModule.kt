package org.kimp.tfs.hw7.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.kimp.tfs.hw7.utils.ZulipAuthInterceptor
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @OptIn(ExperimentalSerializationApi::class)
    fun provideRetrofit(
        client: OkHttpClient
    ) = Retrofit.Builder()
        .baseUrl("https://tinkoff-android-spring-2023.zulipchat.com/")
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .client(client)
        .build()


    @Provides
    fun provideOkHttpClient() = OkHttpClient.Builder()
        .addInterceptor(ZulipAuthInterceptor())
        .build()


    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
}
