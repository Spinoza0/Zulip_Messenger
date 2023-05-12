package com.spinoza.messenger_tfs.util

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class MockRequestDispatcher(
    private val universalPaths: List<String>,
    private val redirectPath: String,
) : Dispatcher() {

    private val responses: MutableMap<String, MockResponse> = mutableMapOf()

    override fun dispatch(request: RecordedRequest): MockResponse {
        val path = request.path ?: throw RuntimeException("Invalid path")
        if (path.startsWith(redirectPath)) {
            val response = responses[REDIRECT_DATA_KEY]
                ?: throw RuntimeException("Invalid data")
            responses[REDIRECT_FROM_KEY] = response
        }
        val key = universalPaths.getUniversalKey(path) ?: path
        return responses[key] ?: MockResponse().setResponseCode(404)
    }

    fun returnsForPath(path: String, response: MockResponse.() -> MockResponse) {
        val key = universalPaths.getUniversalKey(path) ?: path
        responses[key] = response(MockResponse())
    }

    private fun List<String>.getUniversalKey(path: String?): String? {
        if (path == null) return null
        var result: String? = null
        this.forEach { key ->
            if (path.startsWith(key)) {
                result = key
            }
        }
        return result
    }

    companion object {

        const val REDIRECT_FROM_KEY = "/api/v1/messages?num_before"
        const val REDIRECT_DATA_KEY = "redirect"
    }
}