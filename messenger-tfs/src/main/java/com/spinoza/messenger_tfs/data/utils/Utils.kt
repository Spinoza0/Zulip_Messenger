package com.spinoza.messenger_tfs.data.utils

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.spinoza.messenger_tfs.data.network.ZulipApiService
import com.spinoza.messenger_tfs.data.network.ZulipApiService.Companion.RESULT_SUCCESS
import com.spinoza.messenger_tfs.data.network.ZulipResponse
import com.spinoza.messenger_tfs.data.network.model.message.NarrowOperator
import com.spinoza.messenger_tfs.data.network.model.message.NarrowOperatorItemDto
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.RepositoryError
import com.spinoza.messenger_tfs.domain.network.AppAuthKeeper
import com.spinoza.messenger_tfs.domain.network.BaseUrlProvider
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Route
import retrofit2.Response
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

suspend fun <R> runCatchingNonCancellation(block: suspend () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}

fun MessagesFilter.createNarrowJsonForMessages(): String {
    val narrow = mutableListOf<NarrowOperatorItemDto>()
    if (channel.name.isNotEmpty()) {
        narrow.add(
            NarrowOperatorItemDto(NarrowOperator.STREAM.value, channel.name)
        )
    }
    if (topic.name.isNotEmpty()) {
        narrow.add(NarrowOperatorItemDto(NarrowOperator.TOPIC.value, topic.name))
    }
    return Json.encodeToString(narrow)
}

fun MessagesFilter.createNarrowJsonForEvents(): String {
    val narrow = mutableListOf<List<String>>()
    if (channel.name.isNotEmpty()) {
        narrow.add(listOf(NarrowOperator.STREAM.value, channel.name))
    }
    if (topic.name.isNotEmpty()) {
        narrow.add(listOf(NarrowOperator.TOPIC.value, topic.name))
    }
    return Json.encodeToString(narrow)
}

fun MessagesFilter.isEqualTopicName(otherName: String): Boolean {
    return topic.name.equals(otherName, true)
}

inline fun <reified T> Response<T>?.getBodyOrThrow(): T {
    return this?.body() ?: throw RuntimeException("Empty response body")
}

inline fun <reified T> apiRequest(apiCall: () -> ZulipResponse): T {
    val result = apiCall.invoke()
    if (result.result != RESULT_SUCCESS) {
        throw RepositoryError(result.msg)
    }
    return result as T
}

fun BaseUrlProvider.createApiService(authKeeper: AppAuthKeeper, json: Json): ZulipApiService {
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
    val slash = if (this.value.endsWith(SLASH)) EMPTY_STRING else SLASH
    val retrofit = Retrofit.Builder()
        .baseUrl("${this.value}${slash}api/v1/")
        .addConverterFactory(json.asConverterFactory(contentType))
        .client(okHttpClient)
        .build()
    return retrofit.create(ZulipApiService::class.java)
}

private const val MEDIA_TYPE_JSON = "application/json"
private const val SLASH = "/"
private const val EMPTY_STRING = ""
private const val TIME_OUT_SECONDS = 15L