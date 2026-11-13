package com.example.syntax_camping

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Intent tests for the campsite owner's home screen, starting from the bottom-bar tab.
 * Every test starts with this device's user owning no campsites. Tests that need campsites
 * create them first and then start the app, because the app reads the user once at launch.
 * Everything a test creates is deleted afterwards and the owner's list is put back.
 */
@RunWith(AndroidJUnit4::class)
class CampsiteOwnerHomeTest {
    // Not createAndroidComposeRule<MainActivity>(): that launches the app before @Before can set up the data
    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private val db = Database()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var deviceId: String
    private var originalOwnerData: Map<String, Any>? = null
    private var scenario: ActivityScenario<MainActivity>? = null
    private val runTag = UUID.randomUUID().toString().take(8)
    private val seededCampsiteIds = mutableListOf<String>()
    private val uiCampsiteName =
        "UI Camp $runTag"   // the campsite made through the form in one test
    private val emptyOwner = mapOf(
        "lotteryIds" to emptyList<String>(),
        "ownerCampsiteIds" to emptyList<String>()
    )

    private class SeededCampsite(val id: String, val name: String, val location: String)

    // ---------- setup and cleanup ----------

    @Before
    fun resetOwnerToEmpty() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        deviceId = db.getDeviceId(context)

        // Brand-new device: create the user now, or MainActivity would create it during a test and wipe ownerData
        val userRef = firestore.collection("users").document(deviceId)
        if (!Tasks.await(userRef.get(), 15, TimeUnit.SECONDS).exists()) {
            Tasks.await(
                userRef.set(
                    mapOf(
                        "displayName" to "Test Owner",
                        "displayEmail" to "test@example.com",
                        "displayPhoneNumber" to "0000000000"
                    )
                ),
                15, TimeUnit.SECONDS
            )
        }

        // Remember what this user owns now, then start every test owning nothing
        originalOwnerData = Tasks.await(ownerRef().get(), 15, TimeUnit.SECONDS).data
        Tasks.await(ownerRef().set(emptyOwner), 15, TimeUnit.SECONDS)
    }

    @After
    fun cleanUp() {
        scenario?.close()
        try {
            for (id in seededCampsiteIds) {
                Tasks.await(
                    firestore.collection("campsites").document(id).delete(),
                    15,
                    TimeUnit.SECONDS
                )
            }
            val made = Tasks.await(
                firestore.collection("campsites").whereEqualTo("name", uiCampsiteName).get(),
                15, TimeUnit.SECONDS
            )
            for (doc in made.documents) {
                Tasks.await(doc.reference.delete(), 15, TimeUnit.SECONDS)
            }
            Tasks.await(ownerRef().set(originalOwnerData ?: emptyOwner), 15, TimeUnit.SECONDS)
        } catch (e: Throwable) {
            android.util.Log.e("CLEANUP", "Cleanup failed", e)
        }
    }

    // ---------- helpers ----------

    private fun ownerRef() = firestore.collection("ownerData").document(deviceId)

    private fun launchApp() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    /** Taps the owner tab, then waits for the screen to appear and its loading spinner to go away. */
    private fun openOwnerHome() {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag("tab_campsiteshome").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("tab_campsiteshome").performClick()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag("ownerHomeScreen").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithTag("ownerLoading").fetchSemanticsNodes().isEmpty()
        }
    }

    private fun waitForText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * Creates a campsite owned by this user (optionally already in a lottery) and waits until Firestore has saved it.
     * The fields are the same ones Database.createNewCampsite writes. If they change, update them here.
     */
    private fun seedOwnedCampsite(label: String, lotteryId: String = ""): SeededCampsite {
        val id = "test_${runTag}_${UUID.randomUUID().toString().take(6)}"
        seededCampsiteIds.add(id)   // registered first, so cleanup still runs if a step below fails
        val campsite = SeededCampsite(id, "Seed $label $runTag", "Seed Location $label")
        Tasks.await(
            firestore.collection("campsites").document(id).set(
                mapOf(
                    "name" to campsite.name,
                    "location" to campsite.location,
                    "description" to "Created by a test",
                    "maxOccupancy" to 4,
                    "tags" to "Test",
                    "images" to "",
                    "lotteryId" to lotteryId
                )
            ),
            15, TimeUnit.SECONDS
        )
        // link it to this user, the same way the create form does
        Tasks.await(ownerRef().update("ownerCampsiteIds", FieldValue.arrayUnion(id)), 15, TimeUnit.SECONDS)
        return campsite
    }




    // ---------- the tests ----------

    /** Owner with no campsites sees the empty state. */
    @Test
    fun ownerHomeWithNoCampsitesShowsEmptyState() {
        launchApp()
        openOwnerHome()

        composeTestRule
            .onNodeWithText("No campsites found.")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("addCampsiteButton")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Your Campsites Info")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithTag("ownerCampsiteList")
            .assertDoesNotExist()
    }

    /** Owner with a campsite sees the header, the lottery section and the campsite list. */
    @Test
    fun ownerHomeWithCampsiteShowsHeaderListAndLotterySection() {
        val campsite = seedOwnedCampsite("D")
        launchApp()
        openOwnerHome()
        waitForText(campsite.name)

        composeTestRule
            .onNodeWithText("Your Campsites Info")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("ownerThreeDotsMenu")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("OwnerLotterySection")
            .assertExists()

        composeTestRule
            .onNodeWithTag("ownerCampsiteList")
            .assertExists()

        composeTestRule
            .onNodeWithText("No campsites found.")
            .assertDoesNotExist()

    }



    /** With no campsites there is no way to add a lottery: the menu that holds "Add Lottery" isn't shown. */
    @Test
    fun addLotteryIsNotOfferedWhenOwnerHasNoCampsites() {
        launchApp()
        openOwnerHome()

        composeTestRule
            .onNodeWithTag("ownerThreeDotsMenu")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithTag("addLotteryMenuItem")
            .assertDoesNotExist()
    }



    /** Adding a campsite from the empty state: fill the form, save, and it appears on the list. */
    @Test
    fun addCampsiteFromEmptyStateAppearsOnList() {
        launchApp()
        openOwnerHome()
        composeTestRule
            .onNodeWithTag("addCampsiteButton")
            .performClick()

        composeTestRule
            .onNodeWithTag("campsite_name_input")
            .performTextInput(uiCampsiteName)

        composeTestRule
            .onNodeWithTag("campsite_location_input")
            .performTextInput("Banff Park")

        composeTestRule
            .onNodeWithTag("campsite_MaximumOccupant_input")
            .performTextInput("10")

        composeTestRule
            .onNodeWithTag("campsite_description_input")
            .performTextInput("A cozy campsite in the mountains.")

        composeTestRule
            .onNodeWithTag("campsite_Tag_input")
            .performScrollTo()
            .performTextInput("Scenic")

        composeTestRule
            .onNodeWithTag("add_tag_button")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithTag("create_campsite_button")
            .performScrollTo()
            .performClick()

        // wait until we have left the form, then for the new card
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithTag("create_campsite_button")
                .fetchSemanticsNodes().isEmpty()
        }

        waitForText(uiCampsiteName)

        composeTestRule
            .onNodeWithText("Your Campsites Info")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(uiCampsiteName)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("No campsites found.")
            .assertDoesNotExist()
    }

    /** With a campsite: "Add Lottery" opens the lottery form, and the dropdown offers that campsite. */
    @Test
    fun addLotteryWithCampsiteOpensFormAndOffersCampsite() {
        val campsite = seedOwnedCampsite("A")
        launchApp()
        openOwnerHome()
        waitForText(campsite.name)

        composeTestRule
            .onNodeWithTag("ownerThreeDotsMenu")
            .performClick()

        composeTestRule
            .onNodeWithTag("addLotteryMenuItem")
            .performClick()

        composeTestRule
            .onNodeWithText("Create Lottery")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Campsites in Lottery")
            .performScrollTo()
            .assertIsDisplayed()

        // open the dropdown, pick the campsite, and it is listed under "Campsites in Lottery"
        composeTestRule
            .onNodeWithText("Add a campsite...")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithText(campsite.name)
            .performClick()

        composeTestRule
            .onNodeWithText(campsite.name)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /** Every campsite already has a lottery: "Add Lottery" shows the "No Lottery Slot Available" dialog. */
    @Test
    fun addLotteryWhenAllCampsitesHaveLotteriesShowsNoSlotDialog() {
        val campsite = seedOwnedCampsite(
            "B",
            lotteryId = "test_lottery_$runTag")
        launchApp()
        openOwnerHome()
        waitForText(campsite.name)

        composeTestRule
            .onNodeWithTag("ownerThreeDotsMenu")
            .performClick()

        composeTestRule
            .onNodeWithTag("addLotteryMenuItem")
            .performClick()

        composeTestRule
            .onNodeWithTag("noLotteryDialog")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("All your campsites are already in lotteries.")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("noLotteryDialogOk")
            .performClick()

        composeTestRule
            .onNodeWithTag("noLotteryDialog")
            .assertDoesNotExist()
    }


    /** Tap a campsite card to see its details, then "Update Campsite" opens the form filled in. */
    @Test
    fun tappingCampsiteCardShowsDetailsThenPrefilledEditForm() {
        val campsite = seedOwnedCampsite("C")
        launchApp()
        openOwnerHome()
        waitForText(campsite.name)

        composeTestRule
            .onNodeWithText(campsite.name)
            .performClick()

        composeTestRule
            .onNodeWithText("Update Campsite")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(campsite.name)
            .assertIsDisplayed()

        // the details screen shows the location with a leading space, so match part of the text
        composeTestRule
            .onNodeWithText(
                campsite.location,
                substring = true)
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Update Campsite")
            .performClick()

        composeTestRule
            .onNodeWithTag("campsite_name_input")
            .assertTextContains(campsite.name)

        composeTestRule
            .onNodeWithTag("campsite_location_input")
            .assertTextContains(campsite.location)

        composeTestRule
            .onNodeWithText("update campsite")
            .performScrollTo()
            .assertIsDisplayed()

    }




}