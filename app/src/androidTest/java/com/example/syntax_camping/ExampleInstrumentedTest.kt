package com.example.syntax_camping

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.compose.rememberNavController
import com.example.syntax_camping.screens.ownerScreens.MyCampsitesScreen
import com.example.syntax_camping.screens.ownerScreens.OwnerCampsiteDetailsScreen
import com.example.syntax_camping.ui.theme.SyntaxcampingTheme
import java.util.concurrent.atomic.AtomicReference

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
            rating = 3.5
        )
        val second = Campsite(
            documentId = "list-second",
            name = "Mountain Camp",
            location = "Banff, AB",
            description = "A campsite near the mountains.",
            maxOcc = 6,
            tags = mutableListOf("Hiking"),
            rating = 4.2
        )
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
            rating = 4.6
        )
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
        composeRule.onNodeWithText("4.6/5").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Update Campsite").performScrollTo().performClick()
        assertSame(campsite, edited.get())
    }
}