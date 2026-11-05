package com.example.syntax_camping

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.syntax_camping.UserScreens.APP_GUIDELINES_ROUTE
import com.example.syntax_camping.UserScreens.AppGuidelinesScreen
import com.example.syntax_camping.UserScreens.SettingsScreen
import com.example.syntax_camping.ui.theme.SyntaxcampingTheme

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.syntax_camping", appContext.packageName)
    }

    /** US 01.11.01: open the guidelines from Settings, scroll, then return. */
    @Test
    fun settingsGuidelines_opensScrollsAndReturns() {
        composeRule.setContent {
            SyntaxcampingTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "Settings") {
                    composable("Settings") { SettingsScreen(navController) }
                    composable(APP_GUIDELINES_ROUTE) { AppGuidelinesScreen(navController) }
                }
            }
        }

        composeRule.onNodeWithText("Enable notifications").assertIsOff().performClick().assertIsOn()
        composeRule.onNodeWithText("Remember my device so I don’t have to sign in again")
            .assertIsOn().performClick().assertIsOff()
        composeRule.onNodeWithText("See app Policy and Guidelines").performScrollTo().performClick()
        composeRule.onNodeWithText("Privacy Policy").assertIsDisplayed()
        composeRule.onNodeWithText("Data Retention").performScrollTo().performClick()
        composeRule.onNodeWithText("Avoid submitting information", substring = true)
            .performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithContentDescription("back button").performClick()
        composeRule.onNodeWithText("See app Policy and Guidelines").assertIsDisplayed()
        composeRule.onNodeWithText("Enable notifications").performScrollTo().assertIsOn()
        composeRule.onNodeWithText("Remember my device so I don’t have to sign in again").assertIsOff()
    }
}
