package com.spinoza.messenger_tfs

import androidx.test.ext.junit.rules.activityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.spinoza.messenger_tfs.data.network.BaseUrlProviderImpl
import com.spinoza.messenger_tfs.presentation.feature.app.MainActivity
import com.spinoza.messenger_tfs.screen.ChannelsPageScreen
import com.spinoza.messenger_tfs.screen.MessagesScreen
import com.spinoza.messenger_tfs.util.MockRequestDispatcher
import com.spinoza.messenger_tfs.util.loadFromAssets
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
        BaseUrlProviderImpl.value = mockServer.url("/").toString()
    }

    @Test
    fun openMockMessagesScreen() = run {
        val channelsPageScreen = ChannelsPageScreen()
        val messagesScreen = MessagesScreen()
        mockServer.dispatcher = MockRequestDispatcher().apply {
            returnsForPath("users/me/subscriptions")
            { setBody(loadFromAssets("streams_list.json")) }
            returnsForPath("users/me/380669/topics")
            { setBody(loadFromAssets("topics_list.json")) }
            returnsForPath("messages")
            { setBody(loadFromAssets("messages_list.json")) }
        }

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
}