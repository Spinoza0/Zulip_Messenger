package com.spinoza.messenger_tfs.util

import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.mockwebserver.MockWebServer

fun loadFromAssets(filePath: String) =
    InstrumentationRegistry.getInstrumentation().context.resources.assets.open(filePath).use {
        it.bufferedReader().readText()
    }


fun MockWebServer.setupDispatcher(
    type: ServerType = ServerType.WITH_MESSAGES,
    redirectPath: String = "no_value",
    redirectData: String = "messages_list.json",
) {
    val universalPaths = listOf(
        MockRequestDispatcher.REDIRECT_FROM_KEY,
        "/api/v1/register",
        "/api/v1/fetch_api_key"
    )
    val dispatcher = MockRequestDispatcher(universalPaths, redirectPath).apply {
        returnsForPath("/api/v1/fetch_api_key")
        { setBody(loadFromAssets("fetch_api_key.json")) }
        returnsForPath("/api/v1/users/me")
        { setBody(loadFromAssets("own_user.json")) }
        returnsForPath("/api/v1/users/me/subscriptions")
        { setBody(loadFromAssets("streams_list.json")) }
        returnsForPath("/api/v1/users/me/380669/topics")
        { setBody(loadFromAssets("topics_list.json")) }
        returnsForPath("/api/v1/users/604180/presence")
        { setBody(loadFromAssets("default.json")) }
        returnsForPath("/api/v1/register")
        { setBody(loadFromAssets("default.json")) }
        returnsForPath(MockRequestDispatcher.REDIRECT_DATA_KEY)
        { setBody(loadFromAssets(redirectData)) }
        returnsForPath("/api/v1/users/604180/subscriptions/380669")
        { setBody(loadFromAssets("subscription_status.json")) }
    }
    when (type) {
        ServerType.WITH_MESSAGES ->
            dispatcher.returnsForPath(MockRequestDispatcher.REDIRECT_FROM_KEY)
            { setBody(loadFromAssets("messages_list.json")) }

        ServerType.WITHOUT_MESSAGES ->
            dispatcher.returnsForPath(MockRequestDispatcher.REDIRECT_FROM_KEY)
            { setBody(loadFromAssets("empty_messages_list.json")) }

        ServerType.WITH_GETTING_MESSAGES_ERROR ->
            dispatcher.returnsForPath(MockRequestDispatcher.REDIRECT_FROM_KEY) { setBody("[]") }
    }
    this.dispatcher = dispatcher
}

enum class ServerType { WITH_MESSAGES, WITHOUT_MESSAGES, WITH_GETTING_MESSAGES_ERROR }