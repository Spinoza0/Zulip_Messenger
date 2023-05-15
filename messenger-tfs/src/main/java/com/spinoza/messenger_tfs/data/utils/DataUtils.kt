package com.spinoza.messenger_tfs.data.utils

import com.spinoza.messenger_tfs.data.network.apiservice.ZulipApiService.Companion.RESULT_SUCCESS
import com.spinoza.messenger_tfs.data.network.apiservice.ZulipResponse
import com.spinoza.messenger_tfs.data.network.model.message.NarrowOperator
import com.spinoza.messenger_tfs.data.network.model.message.NarrowOperatorItemDto
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.RepositoryError
import com.spinoza.messenger_tfs.domain.util.getText
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.Response

suspend fun <R> runCatchingNonCancellation(block: suspend () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
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

inline fun <reified T> Response<T>?.getBodyOrThrow(): T {
    return this?.body() ?: throw RuntimeException("Empty response body")
}

inline fun <reified T> apiRequest(apiCall: () -> ZulipResponse): T {
    var errorResult: Throwable? = null
    runCatching {
        val result = apiCall.invoke()
        if (result.result != RESULT_SUCCESS) {
            throw RepositoryError(result.msg)
        }
        return result as T
    }.onFailure { error ->
        errorResult = error
        if (error is retrofit2.HttpException) {
            throw RepositoryError(error.getText())
        }
    }
    throw Throwable(errorResult)
}