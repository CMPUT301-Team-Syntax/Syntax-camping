package com.example.syntax_camping

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.syntax_camping.screens.OwnerCampsitesNavigation
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import org.junit.Rule
import org.junit.Test

/** Checks real app navigation with an injected data source instead of Firebase. */
class OwnerCampsiteDataRenderingTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun listOpensTheSelectedRecordAndReloadsItsData() {
        val backend = AtomicReference(listOf(
            campsite("first", "First live campsite", "First description"),
            campsite("second", "Second live campsite", "Original description")
        ))
        val loader: suspend () -> List<Campsite> = { backend.get() }
        compose.setContent { OwnerCampsitesNavigation(loadCampsites = loader) }
        compose.onNodeWithText("First live campsite").assertIsDisplayed()
        compose.onNodeWithText("Second live campsite").assertIsDisplayed()

        // A later response changes the selected record, not its stable ID.
        backend.set(listOf(
            campsite("first", "First live campsite", "First description"),
            campsite("second", "Second live campsite", "Updated backend description")
        ))
        compose.onNodeWithText("Second live campsite").performClick()
        compose.onNodeWithText("Updated backend description").performScrollTo().assertIsDisplayed()

        compose.onNodeWithContentDescription("back button").performClick()
        compose.onNodeWithText("First live campsite").assertIsDisplayed()
        compose.onNodeWithText("Second live campsite").assertIsDisplayed()
    }

    @Test
    fun failedLoadCanBeRetriedWithoutShowingSampleCampsites() {
        val attempts = AtomicInteger()
        val loader: suspend () -> List<Campsite> = {
            if (attempts.incrementAndGet() == 1) throw IOException("Offline test")
            listOf(campsite("recovered", "Recovered campsite", "Loaded after retry"))
        }
        compose.setContent { OwnerCampsitesNavigation(loadCampsites = loader) }
        compose.onNodeWithText("Offline test").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        compose.onNodeWithText("Recovered campsite").assertIsDisplayed()
    }

    @Test
    fun refreshReplacesAnEmptyListWithNewBackendData() {
        val backend = AtomicReference<List<Campsite>>(emptyList())
        val loader: suspend () -> List<Campsite> = { backend.get() }
        compose.setContent { OwnerCampsitesNavigation(loadCampsites = loader) }
        compose.onNodeWithText("No campsites available.").assertIsDisplayed()

        backend.set(listOf(campsite("new", "New database campsite", "New description")))
        compose.onNodeWithText("Refresh").performClick()
        compose.onNodeWithText("New database campsite").assertIsDisplayed()
        compose.onNodeWithText("New database campsite").performClick()
        compose.onNodeWithText("New description").performScrollTo().assertIsDisplayed()
    }

    private fun campsite(id: String, name: String, description: String) = Campsite(
        documentId = id,
        name = name,
        location = "Test location",
        description = description,
        maxOcc = 6,
        tags = mutableListOf("River"),
        rating = 4.2
    )
}
