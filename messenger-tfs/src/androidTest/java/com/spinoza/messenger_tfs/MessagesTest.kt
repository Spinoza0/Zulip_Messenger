package com.spinoza.messenger_tfs

import androidx.test.ext.junit.rules.activityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.spinoza.messenger_tfs.presentation.feature.app.MainActivity
import com.spinoza.messenger_tfs.screen.ChannelsPageScreen
import com.spinoza.messenger_tfs.screen.MessagesScreen
import com.spinoza.messenger_tfs.util.ServerType
import com.spinoza.messenger_tfs.util.setupDispatcher
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MessagesTest : TestCase() {

    @get:Rule
    val activityRule = activityScenarioRule<MainActivity>()

    @get:Rule
    val mockServer = MockWebServer().apply { start(BuildConfig.MOCKWEBSERVER_PORT) }

    @Test
    fun shouldOpenNotEmptyMessagesScreen() = run {
        mockServer.setupDispatcher()
        val channelsPageScreen = ChannelsPageScreen()
        val messagesScreen = MessagesScreen()

        step("Click on first channel") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.ChannelScreenItem>(0)
            { arrowArea.click() }
        }
        step("Click on first topic") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.TopicScreenItem>(1)
            { topic.click() }
        }
        step("Messages screen is not empty") {
            messagesScreen.messagesList.isVisible()
            assertEquals(true, messagesScreen.messagesList.getSize() > 0)
        }
    }

    @Test
    fun shouldOpenEmptyMessagesScreen() = run {
        mockServer.setupDispatcher(ServerType.WITHOUT_MESSAGES)
        val channelsPageScreen = ChannelsPageScreen()
        val messagesScreen = MessagesScreen()

        step("Click on first channel") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.ChannelScreenItem>(0)
            { arrowArea.click() }
        }
        step("Click on second topic") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.TopicScreenItem>(2)
            { topic.click() }
        }
        step("Messages screen is empty") {
            messagesScreen.messagesList.isVisible()
            messagesScreen.messagesList.hasSize(0)
        }
    }

    @Test
    fun shouldOpenMessagesScreenWithError() = run {
        mockServer.setupDispatcher(ServerType.WITH_GETTING_MESSAGES_ERROR)
        val channelsPageScreen = ChannelsPageScreen()
        val messagesScreen = MessagesScreen()

        step("Click on first channel") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.ChannelScreenItem>(0)
            { arrowArea.click() }
        }
        step("Click on third topic") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.TopicScreenItem>(3)
            { topic.click() }
        }
        step("Error is showing") {
            messagesScreen.errorMessage.isVisible()
        }
    }

    @Test
    fun shouldLongClickOnMessageOpensPopupMenu() = run {
        mockServer.setupDispatcher()
        val channelsPageScreen = ChannelsPageScreen()
        val messagesScreen = MessagesScreen()

        step("Open messages screen") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.ChannelScreenItem>(0)
            { arrowArea.click() }
            channelsPageScreen.channels.childAt<ChannelsPageScreen.TopicScreenItem>(1)
            { topic.click() }
        }
        step("Long click on message opens popup menu") {
            messagesScreen.messagesList.childAt<MessagesScreen.MessageItem>(1)
            { longClick() }
            messagesScreen.itemAddReaction.isVisible()
            messagesScreen.itemCopyToClipboard.isVisible()
        }
    }

    @Test
    fun shouldMessageWithReactionsIsVisible() = run {
        mockServer.setupDispatcher()
        val channelsPageScreen = ChannelsPageScreen()
        val messagesScreen = MessagesScreen()

        step("Open messages screen") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.ChannelScreenItem>(0)
            { arrowArea.click() }
            channelsPageScreen.channels.childAt<ChannelsPageScreen.TopicScreenItem>(1)
            { topic.click() }
        }
        step("Message with reactions is displayed") {
            messagesScreen.messagesList.childAt<MessagesScreen.MessageItem>(1)
            { this.iconAddReaction.isDisplayed() }
        }
    }

    @Test
    fun shouldMessageWithoutReactionsIsVisible() = run {
        mockServer.setupDispatcher()
        val channelsPageScreen = ChannelsPageScreen()
        val messagesScreen = MessagesScreen()

        step("Open messages screen") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.ChannelScreenItem>(0)
            { arrowArea.click() }
            channelsPageScreen.channels.childAt<ChannelsPageScreen.TopicScreenItem>(1)
            { topic.click() }
        }
        step("Message without reactions is visible") {
            messagesScreen.messagesList.childAt<MessagesScreen.MessageItem>(2)
            { this.iconAddReaction.isNotDisplayed() }
        }
    }

    @Test
    fun shouldMessageWithOwnUserReactionIsVisible() = run {
        mockServer.setupDispatcher()
        val channelsPageScreen = ChannelsPageScreen()
        val messagesScreen = MessagesScreen()

        step("Open messages screen") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.ChannelScreenItem>(0)
            { arrowArea.click() }
            channelsPageScreen.channels.childAt<ChannelsPageScreen.TopicScreenItem>(1)
            { topic.click() }
        }
        step("Message with own user reaction is displayed") {
            messagesScreen.messagesList.childAt<MessagesScreen.MessageItem>(4) {
                this.ownReaction.isDisplayed()
            }
        }
    }

    @Test
    fun shouldMessagesIsGroupedByDate() = run {
        mockServer.setupDispatcher()
        val channelsPageScreen = ChannelsPageScreen()
        val messagesScreen = MessagesScreen()

        step("Open messages screen") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.ChannelScreenItem>(0)
            { arrowArea.click() }
            channelsPageScreen.channels.childAt<ChannelsPageScreen.TopicScreenItem>(1)
            { topic.click() }
        }
        step("Messages is grouped by date") {
            messagesScreen.messagesList.childAt<MessagesScreen.MessageItem>(0)
            { this.messageDate.isDisplayed() }
            messagesScreen.messagesList.childAt<MessagesScreen.MessageItem>(3)
            { this.messageDate.isDisplayed() }
        }
    }

    @Test
    fun shouldClickOnUserReactionAddsReaction() = run {
        mockServer.setupDispatcher(
            redirectPath = "/api/v1/messages/346542882",
            redirectData = "changed_346542882_message.json"
        )
        val channelsPageScreen = ChannelsPageScreen()
        val messagesScreen = MessagesScreen()
        val messageIndex = 1

        step("Open messages screen") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.ChannelScreenItem>(0)
            { arrowArea.click() }
            channelsPageScreen.channels.childAt<ChannelsPageScreen.TopicScreenItem>(1)
            { topic.click() }
        }
        step("The message contains only other user's reactions") {
            messagesScreen.messagesList.childAt<MessagesScreen.MessageItem>(messageIndex) {
                this.userReaction.isDisplayed()
                this.ownReaction.doesNotExist()
            }
        }
        step("Add own reaction") {
            messagesScreen.messagesList.childAt<MessagesScreen.MessageItem>(messageIndex)
            { this.userReaction.click() }
        }
        step("The message contains own user reaction") {
            messagesScreen.messagesList.childAt<MessagesScreen.MessageItem>(messageIndex) {
                this.ownReaction.isDisplayed()
                this.userReaction.doesNotExist()
            }
        }
    }

    @Test
    fun shouldClickOnOwnReactionDeletesReaction() = run {
        mockServer.setupDispatcher(
            redirectPath = "/api/v1/messages/351668250",
            redirectData = "changed_351668250_message.json"
        )
        val channelsPageScreen = ChannelsPageScreen()
        val messagesScreen = MessagesScreen()
        val messageIndex = 4

        step("Open messages screen") {
            channelsPageScreen.channels.childAt<ChannelsPageScreen.ChannelScreenItem>(0)
            { arrowArea.click() }
            channelsPageScreen.channels.childAt<ChannelsPageScreen.TopicScreenItem>(1)
            { topic.click() }
        }
        step("The message contains own user reaction") {
            messagesScreen.messagesList.childAt<MessagesScreen.MessageItem>(messageIndex) {
                this.ownReaction.isDisplayed()
                this.userReaction.doesNotExist()
            }
        }
        step("Delete own reaction") {
            messagesScreen.messagesList.childAt<MessagesScreen.MessageItem>(messageIndex)
            { this.ownReaction.click() }
        }
        step("The message does not contain own user reaction") {
            messagesScreen.messagesList.childAt<MessagesScreen.MessageItem>(messageIndex) {
                this.userReaction.isDisplayed()
                this.ownReaction.doesNotExist()
            }
        }
    }
}