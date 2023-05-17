package com.spinoza.messenger_tfs

import androidx.test.ext.junit.rules.activityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.spinoza.messenger_tfs.presentation.feature.app.MainActivity
import com.spinoza.messenger_tfs.screen.ChannelsPageScreen
import com.spinoza.messenger_tfs.util.setupDispatcher
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ChannelsTest : TestCase() {

    @get:Rule
    val activityRule = activityScenarioRule<MainActivity>()

    @get:Rule
    val mockServer = MockWebServer().apply { start(BuildConfig.MOCKWEBSERVER_PORT) }

    @Test
    fun shouldOpenNotEmptyChannelsScreen() = run {
        mockServer.setupDispatcher()
        val channelsPageScreen = ChannelsPageScreen()

        step("Show channels") {
            assertEquals(true, channelsPageScreen.channels.getSize() > 0)
        }
    }

    @Test
    fun shouldShowChannelsTopics() = run {
        mockServer.setupDispatcher()
        val channelsPageScreen = ChannelsPageScreen()

        step("Topics are not visible") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.ChannelScreenItem>(1) {
                arrowArea.isDisplayed()
            }
        }
        step("Click on first channel") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.ChannelScreenItem>(0)
            { arrowArea.click() }
        }
        step("Topics are visible") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.TopicScreenItem>(1) {
                topic.isDisplayed()
            }
        }
    }

    @Test
    fun shouldShowChannelsActionBottomDialog() = run {
        mockServer.setupDispatcher()
        val channelsPageScreen = ChannelsPageScreen()

        step("Bottom dialog is not visible") {
            channelsPageScreen.channelUnsubscribe.doesNotExist()
        }
        step("Long click on first channel") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.ChannelScreenItem>(0)
            { channel.longClick() }
        }
        step("Bottom dialog is visible") {
            channelsPageScreen.channelUnsubscribe.isVisible()
        }
    }
}