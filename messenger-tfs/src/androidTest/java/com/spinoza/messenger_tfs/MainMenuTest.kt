package com.spinoza.messenger_tfs

import androidx.test.ext.junit.rules.activityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.spinoza.messenger_tfs.presentation.feature.app.MainActivity
import com.spinoza.messenger_tfs.screen.MainMenuScreen
import org.junit.Rule
import org.junit.Test

class MainMenuTest : TestCase() {

    @get:Rule
    val activityRule = activityScenarioRule<MainActivity>()

    @Test
    fun bottomNavigationMenu_IsVisible() = run {
        val mainMenuScreen = MainMenuScreen()
        step("Bottom navigation menu is visible") {
            mainMenuScreen.bottomMenu.isVisible()
        }
    }
}