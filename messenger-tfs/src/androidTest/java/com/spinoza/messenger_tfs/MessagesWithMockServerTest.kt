package com.spinoza.messenger_tfs

import androidx.test.ext.junit.rules.activityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.spinoza.messenger_tfs.data.network.ApiServiceProviderImpl
import com.spinoza.messenger_tfs.data.network.AppAuthKeeperImpl
import com.spinoza.messenger_tfs.data.network.BaseUrlProviderImpl
import com.spinoza.messenger_tfs.data.utils.createApiService
import com.spinoza.messenger_tfs.presentation.feature.app.MainActivity
import com.spinoza.messenger_tfs.screen.ChannelsPageScreen
import com.spinoza.messenger_tfs.screen.MessagesScreen
import com.spinoza.messenger_tfs.util.MockRequestDispatcher
import com.spinoza.messenger_tfs.util.loadFromAssets
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MessagesWithMockServerTest : TestCase() {

    @get:Rule
    val mockServer = MockWebServer()

    @get:Rule
    val activityRule = activityScenarioRule<MainActivity>()

    @Before
    fun setUp() {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        BaseUrlProviderImpl.value = mockServer.url("/").toString()
        ApiServiceProviderImpl.value =
            BaseUrlProviderImpl.createApiService(AppAuthKeeperImpl(), json)
        setupMockServerDispatcher()
    }

    @Test
    fun openMockMessagesScreen() = run {
        val channelsPageScreen = ChannelsPageScreen()
        val messagesScreen = MessagesScreen()

        step("Click on first channel") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.ChannelScreenItem>(0) {
                channel.click()
            }
        }
        step("Click on first topic") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.TopicScreenItem>(1) {
                topic.click()
            }
        }
        step("Messages screen is visible") {
            messagesScreen.messagesList.isVisible()
        }
    }

    private fun setupMockServerDispatcher() {
        val universalPaths = listOf("/api/v1/messages", "/api/v1/register")
        mockServer.dispatcher = MockRequestDispatcher(universalPaths).apply {
            returnsForPath("/api/v1/fetch_api_key") { setBody("[]") }
            returnsForPath("/api/v1/users/me")
            { setBody(loadFromAssets("own_user.json")) }
            returnsForPath("/api/v1/users/me/subscriptions")
            { setBody(loadFromAssets("streams_list.json")) }
            returnsForPath("/api/v1/users/me/380669/topics")
            { setBody(loadFromAssets("topics_list.json")) }
            returnsForPath("/api/v1/messages") { setBody(loadFromAssets("messages_list.json")) }
            returnsForPath("/api/v1/users/604180/presence") { setBody("[]") }
            returnsForPath("/api/v1/register")
            { setBody("[]") }
        }
    }
}