package com.example.syntax_camping

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
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
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.example.syntax_camping.screens.ownerScreens.MyCampsitesScreen
import com.example.syntax_camping.screens.ownerScreens.OwnerCampsiteDetailsScreen
import java.util.concurrent.atomic.AtomicReference
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.dataClasses.User

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test for from onwercamspitesdetailsscreenUI components.
 * Will execute on an Android device.
 */
@RunWith(AndroidJUnit4::class)
class OwnerCampsiteUITest {
    @get:Rule
    val composeRule = createComposeRule()

    private val userViewModel: UserViewModel by lazy {
        UserViewModel(ApplicationProvider.getApplicationContext<Application>())
    }

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
                    composable("Settings") { SettingsScreen(navController, userViewModel) }
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

    /** US 02.03.01: shows the supplied campsites and opens the selected record. */
    @Test
    fun myCampsitesScreen_showsCampsitesAndSelectsTheClickedOne() {
        val first = Campsite(
            documentId = "list-first",
            name = "River Camp",
            location = "Edmonton, AB",
            description = "A campsite beside the river.",
            maxOcc = 4,
            tags = mutableListOf("River"),
            rating = 3.5,
            images = mutableListOf() // Fixed compile error
        )
        val second = Campsite(
            documentId = "list-second",
            name = "Mountain Camp",
            location = "Banff, AB",
            description = "A campsite near the mountains.",
            maxOcc = 6,
            tags = mutableListOf("Hiking"),
            rating = 4.2,
            images = mutableListOf() // Fixed compile error
        )
        first.updateRaterList(mutableListOf((3.5).toDouble()))
        second.updateRaterList(mutableListOf((4.2).toDouble()))
        val selected = AtomicReference<Campsite?>(null)

        composeRule.setContent {
            SyntaxcampingTheme {
                MyCampsitesScreen(
                    navController = rememberNavController(),
                    campsites = listOf(first, second),
                    onCampsiteClick = { selected.set(it) }
                )
            }
        }

        composeRule.onNodeWithText(first.name).assertIsDisplayed()
        composeRule.onNodeWithText(first.location).assertIsDisplayed()
        composeRule.onNodeWithText("3.5/5").assertIsDisplayed()
        composeRule.onNodeWithText(second.name).assertIsDisplayed()
        composeRule.onNodeWithText(second.location).assertIsDisplayed()
        composeRule.onNodeWithText(second.name).performClick()
        assertSame(second, selected.get())
    }

    /** US 02.01.04: shows the selected campsite's details and passes it to editing. */
    @Test
    fun ownerCampsiteDetailsScreen_showsDetailsAndEditsTheSelectedCampsite() {
        val campsite = Campsite(
            documentId = "details-camp",
            name = "Forest Camp",
            location = "Jasper, AB",
            description = "A quiet campsite with picnic tables and walking trails.",
            maxOcc = 8,
            tags = mutableListOf("Forest", "Picnic tables"),
            rating = 4.6,
            images = mutableListOf() // Fixed compile error
        )
        campsite.updateRaterList(mutableListOf((4.6).toDouble()))
        val edited = AtomicReference<Campsite?>(null)

        composeRule.setContent {
            SyntaxcampingTheme {
                OwnerCampsiteDetailsScreen(
                    navController = rememberNavController(),
                    campsite = campsite,
                    onEditCampsite = { edited.set(it) }
                )
            }
        }

        composeRule.onNodeWithText(campsite.name).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(campsite.location).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(campsite.description).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Maximum occupants").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("8").assertIsDisplayed()
        composeRule.onNodeWithText("#Forest").assertIsDisplayed()
        composeRule.onNodeWithText("#Picnic tables").assertIsDisplayed()
        composeRule.onNodeWithText("4.6", substring = true).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Update Campsite").performScrollTo().performClick()
        assertSame(campsite, edited.get())
    }
}
