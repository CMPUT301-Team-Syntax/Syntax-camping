package com.example.syntax_camping//package com.example.syntax_camping

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.syntax_camping.UserScreens.SearchHomeScreen
import com.example.syntax_camping.screens.CampsiteListScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteEditingScreen
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class CampsiteAppTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val testCampName = "Pine Haven Camp ${System.currentTimeMillis()}"

    @After
    fun cleanUp() {
        try {
            val snapshot = Tasks.await(
                FirebaseFirestore.getInstance()
                    .collection("campsites")
                    .whereEqualTo("name", testCampName)
                    .get()
            )
            for (doc in snapshot.documents) {
                Tasks.await(doc.reference.delete())
            }
        } catch (e: Exception) {
            android.util.Log.e("CLEANUP", "Cleanup failed", e) // CHANGED: no more silent failure
        }
    }

    @Test
    fun addNewCampsiteTest_and_FilterIt() {

        composeTestRule
            .waitForIdle()

        composeTestRule
            .onNodeWithTag("tab_campsiteshome")
            .performClick()

        composeTestRule.waitForIdle()

        // Wait until the screen finishes loading and shows either the add button or three-dots menu
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("add_campsite_button").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithTag("owner_three_dots_menu").fetchSemanticsNodes().isNotEmpty()
        }

        val isEmptyState = composeTestRule
            .onAllNodesWithTag("add_campsite_button")
            .fetchSemanticsNodes()
            .isNotEmpty()

        if (isEmptyState) {
            // Click the direct empty-state button
            composeTestRule
                .onNodeWithTag("add_campsite_button")
                .performClick()
        } else {
            // Open three-dots menu top right, then click "Add Campsite"
            composeTestRule
                .onNodeWithTag("owner_three_dots_menu")
                .performClick()

            composeTestRule
                .onNodeWithTag("add_campsite_menu_item")
                .performClick()
        }

        // 3. Fill in top fields
        composeTestRule
            .onNodeWithTag("campsite_name_input")
            .performTextInput(testCampName)

        composeTestRule
            .onNodeWithTag("campsite_location_input")
            .performTextInput("Banff Park")

        composeTestRule
            .onNodeWithTag("campsite_MaximumOccupant_input")
            .performTextInput("10")

        composeTestRule
            .onNodeWithTag("campsite_description_input")
            .performTextInput("A cozy campsite in the mountains.")

// 4. Scroll to tag input AND add_tag_button before tapping
        composeTestRule
            .onNodeWithTag("campsite_Tag_input")
            .performScrollTo()// <--- MUST SCROLL FIRST
            .performTextInput("Scenic")

        composeTestRule
            .onNodeWithTag("add_tag_button")
            .performScrollTo()// <--- MUST SCROLL FIRST (so it doesn't click CustomBottomBar!)
            .performClick()

        composeTestRule.waitForIdle()

// 5. Scroll to create button and submit
        composeTestRule
            .onNodeWithTag("create_campsite_button")
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()

// 6. Verify newly created campsite name is displayed (wait for async Firebase save and navigation pop)
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Pine Haven Camp").fetchSemanticsNodes().isNotEmpty()
        }

        // Step 1: wait until we've left the form
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            composeTestRule
                .onAllNodesWithTag("create_campsite_button")
                .fetchSemanticsNodes()
                .isEmpty()
        }

// Step 2: wait for the card to appear on the list
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            composeTestRule
                .onAllNodesWithText("Pine Haven Camp")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }


        composeTestRule
            .onNodeWithTag("tab_search")
            .performClick()



    }
}










//        composeTestRule.setContent {
//            val navController = androidx.navigation.compose.rememberNavController()
//            SearchHomeScreen(navController = navController)
//        }
//        // 1. Type into search bar
//        composeTestRule
//            .onNodeWithTag("search_input_field")
//            .performTextInput("Jasper")
//
//        // 2. Click search icon
//        composeTestRule
//            .onNodeWithTag("search_icon")
//            .performClick()
//        composeTestRule
//            .onNodeWithText("Lakeside Haven")
//            .assertIsDisplayed()
//        composeTestRule
//            .onNodeWithText("Pine Ridge Campground")
//            .assertDoesNotExist()


