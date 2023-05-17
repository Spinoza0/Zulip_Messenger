package com.spinoza.messenger_tfs

import androidx.test.ext.junit.rules.activityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.spinoza.messenger_tfs.presentation.feature.app.MainActivity
import com.spinoza.messenger_tfs.screen.MainMenuScreen
import com.spinoza.messenger_tfs.screen.PeopleScreen
import com.spinoza.messenger_tfs.util.setupDispatcher
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test

class PeopleTest : TestCase() {

    @get:Rule
    val activityRule = activityScenarioRule<MainActivity>()

    @get:Rule
    val mockServer = MockWebServer().apply { start(BuildConfig.MOCKWEBSERVER_PORT) }

    @Test
    fun shouldOpenNotEmptyPeopleScreen() = run {
        mockServer.setupDispatcher()
        val mainMenuScreen = MainMenuScreen()
        val peopleScreen = PeopleScreen()

        step("Open people screen") {
            mainMenuScreen.bottomNavigationView.click()
        }
        step("Users list is visible") {
            peopleScreen.users.isVisible()
        }
    }
}