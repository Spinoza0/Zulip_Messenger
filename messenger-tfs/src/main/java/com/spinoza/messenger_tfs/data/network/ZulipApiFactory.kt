package com.spinoza.messenger_tfs.data.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.*
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object ZulipApiFactory {

    private const val HEADER_AUTHORIZATION = "Authorization"
    private const val MEDIA_TYPE_JSON = "application/json"

    private const val BASE_URL = "https://victoryvalery.zulipchat.com/api/v1/"
    private const val CREDENTIALS_USERNAME = "ivan.sintyurin@gmail.com"
    private const val CREDENTIALS_PASSWORD = "RaINyfjtFHz8KEUFXtXzxPcVVRjaDdrm"

    // private const val BASE_URL = "https://tinkoff-android-spring-2023.zulipchat.com/api/v1/"
    // private const val CREDENTIALS_USERNAME = "spinoza0@gmail.com"
    // private const val CREDENTIALS_PASSWORD = "Tu1s51Gtq1ec02fBd1lhAaOALD0hc2JH"

    private const val TIME_OUT_SECONDS = 15L
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val contentType = MediaType.get(MEDIA_TYPE_JSON)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
        .authenticator { _: Route?, response: Response ->
            val request = response.request()
            if (request.header(HEADER_AUTHORIZATION) != null)
                return@authenticator null
            request.newBuilder().header(
                HEADER_AUTHORIZATION,
                Credentials.basic(CREDENTIALS_USERNAME, CREDENTIALS_PASSWORD)
            ).build()
        }
        .build()

    @OptIn(ExperimentalSerializationApi::class)
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(json.asConverterFactory(contentType))
        .client(okHttpClient)
        .build()

    val apiService: ZulipApiService = retrofit.create(ZulipApiService::class.java)
}