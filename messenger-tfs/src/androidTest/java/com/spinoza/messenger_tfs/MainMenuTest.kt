package com.spinoza.messenger_tfs

import androidx.test.ext.junit.rules.activityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.spinoza.messenger_tfs.presentation.feature.app.MainActivity
import com.spinoza.messenger_tfs.screen.MainMenuScreen
import com.spinoza.messenger_tfs.util.setupDispatcher
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test

class MainMenuTest : TestCase() {

    @get:Rule
    val activityRule = activityScenarioRule<MainActivity>()

    @get:Rule
    val mockServer = MockWebServer().apply { start(BuildConfig.MOCKWEBSERVER_PORT) }

    @Test
    fun shouldOpenMainMenuScreen() = run {
        mockServer.setupDispatcher()
        val mainMenuScreen = MainMenuScreen()

        step("Bottom menu is visible") {
            mainMenuScreen.bottomNavigationView.isVisible()
        }
    }
}