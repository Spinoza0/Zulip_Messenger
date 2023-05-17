package com.spinoza.messenger_tfs

import androidx.test.ext.junit.rules.activityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.spinoza.messenger_tfs.presentation.feature.app.MainActivity
import com.spinoza.messenger_tfs.screen.MainMenuScreen
import com.spinoza.messenger_tfs.screen.PeopleScreen
import com.spinoza.messenger_tfs.screen.ProfileScreen
import com.spinoza.messenger_tfs.util.setupDispatcher
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test

class ProfileTest : TestCase() {

    @get:Rule
    val activityRule = activityScenarioRule<MainActivity>()

    @get:Rule
    val mockServer = MockWebServer().apply { start(BuildConfig.MOCKWEBSERVER_PORT) }

    @Test
    fun shouldOpenProfileScreen() = run {
        mockServer.setupDispatcher()
        val mainMenuScreen = MainMenuScreen()
        val peopleScreen = PeopleScreen()
        val profileScreen = ProfileScreen()

        step("Open people screen") {
            mainMenuScreen.bottomNavigationView.click()
        }
        step("Open profile screen") {
            peopleScreen.users.childAt<PeopleScreen.PeopleScreenItem>(0)
            { textViewName.click() }
        }
        step("Profile screen contains user's name") {
            profileScreen.username.isVisible()
        }
    }
}