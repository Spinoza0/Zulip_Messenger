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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MessagesTest : TestCase() {

    @get:Rule
    val activityRule = activityScenarioRule<MainActivity>()

    @get:Rule
    val mockServer = MockWebServer().apply { start(1234) }

    @Before
    fun setUp() {
        BaseUrlProviderImpl.value = mockServer.url("/").toString()
        ApiServiceProviderImpl.value =
            BaseUrlProviderImpl.createApiService(AppAuthKeeperImpl(), provideJsonConverter())
    }

    @Test
    fun openNotEmptyMessagesScreen() = run {
        setupMockServerDispatcher(ServerType.WITH_MESSAGES)
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
        step("Messages screen is not empty") {
            messagesScreen.messagesList.isVisible()
            assertEquals(true, messagesScreen.messagesList.getSize() > 0)
        }
    }

    @Test
    fun openEmptyMessagesScreen() = run {
        setupMockServerDispatcher(ServerType.WITHOUT_MESSAGES)
        val channelsPageScreen = ChannelsPageScreen()
        val messagesScreen = MessagesScreen()

        step("Click on first channel") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.ChannelScreenItem>(0) {
                channel.click()
            }
        }
        step("Click on second topic") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.TopicScreenItem>(2) {
                topic.click()
            }
        }
        step("Messages screen is empty") {
            messagesScreen.messagesList.isVisible()
            messagesScreen.messagesList.hasSize(0)
        }
    }

    @Test
    fun openMessagesScreenWithError() = run {
        setupMockServerDispatcher(ServerType.WITH_GETTING_MESSAGES_ERROR)
        val channelsPageScreen = ChannelsPageScreen()
        val messagesScreen = MessagesScreen()

        step("Click on first channel") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.ChannelScreenItem>(0) {
                channel.click()
            }
        }
        step("Click on third topic") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.TopicScreenItem>(3) {
                topic.click()
            }
        }
        step("Error is showing") {
            messagesScreen.errorMessage.isVisible()
        }
    }

    @Test
    fun longClickOnMessageOpensChooseReactionDialog() = run {
        setupMockServerDispatcher(ServerType.WITH_MESSAGES)
        val channelsPageScreen = ChannelsPageScreen()
        val messagesScreen = MessagesScreen()

        step("Open messages screen") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.ChannelScreenItem>(0) {
                channel.click()
            }
        }
        step("Click on first topic") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.TopicScreenItem>(1) {
                topic.click()
            }
        }
        step("Long click on message opens choose reaction dialog") {
            messagesScreen.messagesList.childAt<MessagesScreen.MessageItem>(1) {
                longClick()
            }
            messagesScreen.chooseReactionDialogTopLine.isVisible()
        }
    }

    @Test
    fun messageWithReactionsIsVisible() = run {
        setupMockServerDispatcher(ServerType.WITH_MESSAGES)
        val channelsPageScreen = ChannelsPageScreen()
        val messagesScreen = MessagesScreen()

        step("Open messages screen") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.ChannelScreenItem>(0) {
                channel.click()
            }
        }
        step("Click on first topic") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.TopicScreenItem>(1) {
                topic.click()
            }
        }
        step("Message with reactions is visible") {
            messagesScreen.messagesList.childAt<MessagesScreen.MessageItem>(1) {
                this.iconAddReaction.isDisplayed()
            }
        }
    }

    @Test
    fun messageWithoutReactionsIsVisible() = run {
        setupMockServerDispatcher(ServerType.WITH_MESSAGES)
        val channelsPageScreen = ChannelsPageScreen()
        val messagesScreen = MessagesScreen()

        step("Open messages screen") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.ChannelScreenItem>(0) {
                channel.click()
            }
        }
        step("Click on first topic") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.TopicScreenItem>(1) {
                topic.click()
            }
        }
        step("Message without reactions is visible") {
            messagesScreen.messagesList.childAt<MessagesScreen.MessageItem>(2) {
                this.iconAddReaction.isNotDisplayed()
            }
        }
    }

    private fun provideJsonConverter() = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private fun setupMockServerDispatcher(type: ServerType) {
        val universalPaths = listOf(
            "/api/v1/messages",
            "/api/v1/register",
            "/api/v1/fetch_api_key"
        )
        val dispatcher = MockRequestDispatcher(universalPaths).apply {
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
        }
        when (type) {
            ServerType.WITH_MESSAGES -> dispatcher.returnsForPath("/api/v1/messages")
            { setBody(loadFromAssets("messages_list.json")) }

            ServerType.WITHOUT_MESSAGES -> dispatcher.returnsForPath("/api/v1/messages")
            { setBody(loadFromAssets("empty_messages_list.json")) }

            ServerType.WITH_GETTING_MESSAGES_ERROR ->
                dispatcher.returnsForPath("/api/v1/messages") { setBody("[]") }
        }
        mockServer.dispatcher = dispatcher
    }

    enum class ServerType { WITH_MESSAGES, WITHOUT_MESSAGES, WITH_GETTING_MESSAGES_ERROR }
}