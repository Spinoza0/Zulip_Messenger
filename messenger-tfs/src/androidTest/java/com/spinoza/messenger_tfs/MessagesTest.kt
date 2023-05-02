package com.spinoza.messenger_tfs

import androidx.test.ext.junit.rules.activityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.spinoza.messenger_tfs.presentation.feature.app.MainActivity
import com.spinoza.messenger_tfs.screen.ChannelsPageScreen
import com.spinoza.messenger_tfs.screen.MessagesScreen
import org.junit.Rule
import org.junit.Test

class MessagesTest : TestCase() {

    @get:Rule
    val activityRule = activityScenarioRule<MainActivity>()

    @Test
    fun openMessagesScreen() = run {
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
}