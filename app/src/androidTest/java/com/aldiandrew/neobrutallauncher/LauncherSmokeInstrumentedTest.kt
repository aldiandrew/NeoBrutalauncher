package com.aldiandrew.neobrutallauncher

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.fetchSemanticsNodes
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LauncherSmokeInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launcherStartsAndRendersAVisibleComposeRoot() {
        composeRule.waitForIdle()
        composeRule.onRoot().assertIsDisplayed()
        assertFalse("MainActivity unexpectedly finishing", composeRule.activity.isFinishing)
    }

    @Test
    fun firstScreenExposesAtLeastOneAccessibleInteractiveControl() {
        composeRule.waitForIdle()
        val clickableNodes = composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes()
        assertTrue(
            "No interactive Compose controls were exposed to accessibility/test semantics",
            clickableNodes.isNotEmpty()
        )
    }
}
