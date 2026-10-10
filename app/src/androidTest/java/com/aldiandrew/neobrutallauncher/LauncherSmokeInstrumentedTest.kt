package com.aldiandrew.neobrutallauncher

import android.content.Context
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.fetchSemanticsNodes
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LauncherSmokeInstrumentedTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private fun launchConfiguredHome(): ActivityScenario<MainActivity> {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences(
            "neo_brutal_launcher_preferences",
            Context.MODE_PRIVATE
        ).edit()
            .clear()
            .putBoolean("onboarding_completed", true)
            .putInt("home_app_count", 5)
            .putBoolean("home_apps_initialized", true)
            .putStringSet("favorites", emptySet())
            .commit()

        return ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun launcherStartsAndRendersAVisibleComposeRoot() {
        val scenario = launchConfiguredHome()
        try {
            composeRule.waitForIdle()
            composeRule.onRoot().assertIsDisplayed()
            composeRule.onNodeWithText("HOME").assertExists()
            scenario.onActivity { activity ->
                assertTrue("MainActivity unexpectedly finishing", !activity.isFinishing)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun horizontalNavigationReachesAppsAndLivePages() {
        val scenario = launchConfiguredHome()
        try {
            composeRule.waitForIdle()
            composeRule.onNodeWithText("HOME").assertExists()

            composeRule.onRoot().performTouchInput { swipeLeft() }
            composeRule.waitForIdle()
            composeRule.onNodeWithText("APPS").assertExists()

            composeRule.onRoot().performTouchInput { swipeLeft() }
            composeRule.waitForIdle()
            composeRule.onNodeWithText("LIVE").assertExists()
        } finally {
            scenario.close()
        }
    }

    @Test
    fun homeScreenExposesInteractiveControlsThroughComposeSemantics() {
        val scenario = launchConfiguredHome()
        try {
            composeRule.waitForIdle()
            val clickableNodes = composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes()
            assertTrue(
                "No interactive Compose controls were exposed to accessibility/test semantics",
                clickableNodes.isNotEmpty()
            )
        } finally {
            scenario.close()
        }
    }
}
