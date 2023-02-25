package com.spinoza.homework_1.presentation.activity

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinoza.homework_1.R
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun beforeTests() {
        Intents.init()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun afterTests() {
        Intents.release()
        scenario.close()
    }

    @Test
    fun screenContainsButtonStartWork() {
        onView(
            withId(R.id.buttonStartWork)
        ).check(
            matches(
                withText(R.string.get_first_5_contacts)
            )
        )
    }
}