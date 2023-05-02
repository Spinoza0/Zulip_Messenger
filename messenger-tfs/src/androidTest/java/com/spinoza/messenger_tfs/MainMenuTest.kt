package com.spinoza.messenger_tfs

import androidx.test.ext.junit.rules.activityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.spinoza.messenger_tfs.presentation.feature.app.MainActivity
import com.spinoza.messenger_tfs.screen.ChannelsScreen
import com.spinoza.messenger_tfs.screen.MainMenuScreen
import com.spinoza.messenger_tfs.screen.PeopleScreen
import org.junit.Rule
import org.junit.Test

class MainMenuTest : TestCase() {

    @get:Rule
    val activityRule = activityScenarioRule<MainActivity>()

    @Test
    fun channelsScreenIsVisible_ByDefault() = run {
        val mainMenuScreen = MainMenuScreen()
        val channelsScreen = ChannelsScreen()
        step("Bottom navigation menu is visible") {
            mainMenuScreen.bottomMenu.isVisible()
        }
        step("Channels screen is visible") {
            channelsScreen.searchField.isVisible()
            channelsScreen.tabLayout.isVisible()
        }
    }

    @Test
    fun peopleScreen_IsVisible() = run {
        val mainMenuScreen = MainMenuScreen()
        val peopleScreen = PeopleScreen()
        step("Open the people screen") {
            mainMenuScreen.bottomMenu.click()
        }
        step("Users list is visible") {
            peopleScreen.searchField.isVisible()
            peopleScreen.usersList.isVisible()
        }
    }
}