package com.spinoza.messenger_tfs

import androidx.test.espresso.NoMatchingViewException
import androidx.test.ext.junit.rules.activityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.spinoza.messenger_tfs.presentation.feature.app.MainActivity
import com.spinoza.messenger_tfs.screen.ChannelsPageScreen
import com.spinoza.messenger_tfs.screen.MessagesScreen
import com.spinoza.messenger_tfs.util.MockRequestDispatcher
import com.spinoza.messenger_tfs.util.loadFromAssets
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test

class MessagesTest : TestCase() {

    @get:Rule
    val activityRule = activityScenarioRule<MainActivity>()

    @get:Rule
    val mockServer = MockWebServer().apply { start(BuildConfig.MOCKWEBSERVER_PORT) }

    @Test
    fun shouldOpenNotEmptyMessagesScreen() = run {
        setupMockServerDispatcher(ServerType.WITH_MESSAGES)
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
        setupMockServerDispatcher(ServerType.WITHOUT_MESSAGES)
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
        setupMockServerDispatcher(ServerType.WITH_GETTING_MESSAGES_ERROR)
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
        setupMockServerDispatcher(ServerType.WITH_MESSAGES)
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
        setupMockServerDispatcher(ServerType.WITH_MESSAGES)
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
        setupMockServerDispatcher(ServerType.WITH_MESSAGES)
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
        setupMockServerDispatcher(ServerType.WITH_MESSAGES)
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
        setupMockServerDispatcher(ServerType.WITH_MESSAGES)
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
        setupMockServerDispatcher(
            ServerType.WITH_MESSAGES,
            "/api/v1/messages/346542882",
            "changed_346542882_message.json"
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
                assertThrows(NoMatchingViewException::class.java) { this.ownReaction.isNotDisplayed() }
            }
        }
        step("Add own reaction") {
            messagesScreen.messagesList.childAt<MessagesScreen.MessageItem>(messageIndex)
            { this.userReaction.click() }
        }
        step("The message contains own user reaction") {
            messagesScreen.messagesList.childAt<MessagesScreen.MessageItem>(messageIndex) {
                this.ownReaction.isDisplayed()
                assertThrows(NoMatchingViewException::class.java) { this.userReaction.isNotDisplayed() }
            }
        }
    }

    @Test
    fun shouldClickOnOwnReactionDeletesReaction() = run {
        setupMockServerDispatcher(
            ServerType.WITH_MESSAGES,
            "/api/v1/messages/351668250",
            "changed_351668250_message.json"
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
                assertThrows(NoMatchingViewException::class.java) { this.userReaction.isNotDisplayed() }
            }
        }
        step("Delete own reaction") {
            messagesScreen.messagesList.childAt<MessagesScreen.MessageItem>(messageIndex)
            { this.ownReaction.click() }
        }
        step("The message does not contain own user reaction") {
            messagesScreen.messagesList.childAt<MessagesScreen.MessageItem>(messageIndex) {
                this.userReaction.isDisplayed()
                assertThrows(NoMatchingViewException::class.java) { this.ownReaction.isNotDisplayed() }
            }
        }
    }

    private fun setupMockServerDispatcher(
        type: ServerType,
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
        mockServer.dispatcher = dispatcher
    }

    enum class ServerType { WITH_MESSAGES, WITHOUT_MESSAGES, WITH_GETTING_MESSAGES_ERROR }
}