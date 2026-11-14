package com.example.syntax_camping

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.syntax_camping.dataClasses.dateConverterToLong
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Intent tests for the campsite owner's home screen, starting from the bottom-bar tab.
 * Every test starts with this device's user owning no campsites. Tests that need campsites
 * create them first and then start the app, because the app reads the user once at launch.
 * Everything a test creates (campsites, lotteries) is deleted afterwards and the owner's list is put back.
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
    private val oneDayMillis = 24L * 60 * 60 * 1000
    private val seededCampsiteIds = mutableListOf<String>()
    private val createdLotteryNames = mutableListOf<String>()
    private val uiCampsiteName = "UI Camp $runTag"   // the campsite made through the form in one test
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
        // each step on its own, so one failure doesn't skip the rest
        tryCleanup {
            for (id in seededCampsiteIds) {
                Tasks.await(firestore.collection("campsites").document(id).delete(), 15, TimeUnit.SECONDS)
            }
        }
        tryCleanup {
            val made = Tasks.await(
                firestore.collection("campsites").whereEqualTo("name", uiCampsiteName).get(),
                15, TimeUnit.SECONDS
            )
            for (doc in made.documents) {
                Tasks.await(doc.reference.delete(), 15, TimeUnit.SECONDS)
            }
        }
        tryCleanup {
            for (name in createdLotteryNames) {
                val found = Tasks.await(
                    firestore.collection("lotteries").whereEqualTo("name", name).get(),
                    15, TimeUnit.SECONDS
                )
                for (doc in found.documents) {
                    Tasks.await(doc.reference.delete(), 15, TimeUnit.SECONDS)
                }
            }
        }
        tryCleanup {
            Tasks.await(ownerRef().set(originalOwnerData ?: emptyOwner), 15, TimeUnit.SECONDS)
        }
    }

    private fun tryCleanup(step: () -> Unit) {
        try {
            step()
        } catch (e: Throwable) {
            android.util.Log.e("CLEANUP", "A cleanup step failed", e)
        }
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
            .onNodeWithText("No running Lotteries")
            .assertExists()

        composeTestRule
            .onNodeWithTag("OwnerLotterySection")
            .assertExists()

        composeTestRule
            .onNodeWithTag("addLottery")
            .assertExists()

        composeTestRule
            .onNodeWithText("Your Campsites")   // CHANGED: this header is shown once the owner has a campsite
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
            .onNodeWithTag("campsiteNameInput")
            .performTextInput(uiCampsiteName)

        composeTestRule
            .onNodeWithTag("campsiteLocationInput")
            .performTextInput("Banff Park")

        composeTestRule
            .onNodeWithTag("campsiteMaximumOccupantInput")
            .performTextInput("10")

        composeTestRule
            .onNodeWithTag("campsiteDescriptionInput")
            .performTextInput("A cozy campsite in the mountains.")

        composeTestRule
            .onNodeWithTag("campsiteTagInput")
            .performScrollTo()
            .performTextInput("Scenic")

        composeTestRule
            .onNodeWithTag("addTagButton")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithTag("createCampsiteButton")
            .performScrollTo()
            .performClick()

        // wait until we have left the form, then for the new card
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithTag("createCampsiteButton")
                .fetchSemanticsNodes().isEmpty()
        }

        waitForText(uiCampsiteName)

        composeTestRule
            .onNodeWithText("Your Campsites Info")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(uiCampsiteName)
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Banff Park")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("10 people max")
            .assertIsDisplayed()

        // CHANGED: removed the "No lotteries available" check. That text is on the details screen, not on the home list.

        composeTestRule
            .onNodeWithTag("Rating", useUnmergedTree = true)
            .performScrollTo()
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
            lotteryId = "test_lottery_$runTag"
        )
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
                substring = true
            )
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("No lotteries available")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("addLottery")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Update Campsite")
            .performClick()

        composeTestRule
            .onNodeWithTag("campsiteNameInput")
            .assertTextContains(campsite.name)

        composeTestRule
            .onNodeWithTag("campsiteLocationInput")
            .assertTextContains(campsite.location)

        composeTestRule
            .onNodeWithText("update campsite")
            .performScrollTo()
            .assertIsDisplayed()
    }

    // ================= NEW: lotteries =================

    /** An owner with a campsite creates a lottery: it shows on the home screen and the campsite is linked to it. */
    @Test
    fun createdLotteryShowsOnHomeAndIsLinkedToTheCampsite() {
        val campsite = seedOwnedCampsite("A")
        launchApp()
        openOwnerHome()
        waitForText(campsite.name)

        composeTestRule
            .onAllNodesWithTag("lotteryCard")
            .assertCountEquals(0)

        val lotteryName = newLotteryName("one")
        createLotteryFromHome(lotteryName, listOf(campsite))

        waitForLotteryCards(1)
        val lotteryId = waitForLotteryId(lotteryName)
        waitUntilTrue("the campsite to point at the new lottery") {
            campsiteLotteryId(campsite.id) == lotteryId
        }
    }

    /** One campsite, already in a lottery: no second lottery can be added. */
    @Test
    fun noSecondLotteryWhenEveryCampsiteAlreadyHasOne() {
        val campsite = seedOwnedCampsite("A")
        launchApp()
        openOwnerHome()
        waitForText(campsite.name)

        createLotteryFromHome(
            newLotteryName("one"),
            listOf(campsite)
        )

        waitForLotteryCards(1)

        openAddLotteryFromHome()
        composeTestRule
            .onNodeWithTag("noLotteryDialog")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("noLotteryDialogOk")
            .performClick()

        composeTestRule
            .onNodeWithTag("noLotteryDialog")
            .assertDoesNotExist()

        composeTestRule
            .onAllNodesWithTag("lotteryCard")
            .assertCountEquals(1)
    }

    /**
     * Two campsites, one lottery for the first: the form still opens and offers only the second.
     * Once that one has a lottery too, "Add Lottery" is blocked.
     */
    @Test
    fun withTwoCampsitesTheSecondLotteryOffersOnlyTheRemainingCampsite() {
        val a = seedOwnedCampsite("A")
        val b = seedOwnedCampsite("B")
        launchApp()
        openOwnerHome()
        waitForText(a.name)
        waitForText(b.name)

        createLotteryFromHome(
            newLotteryName("first"),
            listOf(a)
        )

        waitForLotteryCards(1)

        openAddLotteryFromHome()   // a campsite is still free, so this opens the form and not the dialog
        composeTestRule
            .onNodeWithTag("createLotteryButton")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Add a campsite...")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithText(b.name)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(a.name)
            .assertDoesNotExist()   // already in a lottery, so not offered

        composeTestRule
            .onNodeWithText(b.name)
            .performClick()

        fillLotteryForm(newLotteryName("second"))
        submitLotteryForm()
        waitForLotteryCards(2)

        openAddLotteryFromHome()
        composeTestRule
            .onNodeWithTag("noLotteryDialog")
            .assertIsDisplayed()
    }

    /** Two campsites in the same lottery: both are linked to it, and no further lottery can be added. */
    @Test
    fun twoCampsitesInTheSameLotteryUseUpTheLotterySlot() {
        val a = seedOwnedCampsite("A")
        val b = seedOwnedCampsite("B")
        launchApp()
        openOwnerHome()
        waitForText(a.name)
        waitForText(b.name)

        val lotteryName = newLotteryName("both")
        createLotteryFromHome(
            lotteryName,
            listOf(a, b))

        waitForLotteryCards(1)   // one lottery, even though two campsites are in it
        val lotteryId = waitForLotteryId(lotteryName)
        waitUntilTrue("both campsites to point at the lottery") {
            campsiteLotteryId(a.id) == lotteryId && campsiteLotteryId(b.id) == lotteryId
        }

        openAddLotteryFromHome()
        composeTestRule
            .onNodeWithTag("noLotteryDialog")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("noLotteryDialogOk")
            .performClick()

        composeTestRule
            .onAllNodesWithTag("lotteryCard")
            .assertCountEquals(1)
    }

    /** "Add Lottery" on the campsite details screen opens the form with that campsite chosen; the new lottery then shows. */
    @Test
    fun addLotteryFromCampsiteDetailsPreselectsTheCampsiteAndShowsTheLottery() {
        val campsite = seedOwnedCampsite("D")
        launchApp()
        openOwnerHome()
        waitForText(campsite.name)

        composeTestRule
            .onNodeWithText(campsite.name)
            .performClick()   // card -> details

        composeTestRule
            .onNodeWithTag("addLottery")
            .performScrollTo()
            .performClick()

        // the form opens with this campsite already listed under "Campsites in Lottery"
        composeTestRule
            .onNodeWithText(campsite.name)
            .performScrollTo()
            .assertIsDisplayed()

        val lotteryName = newLotteryName("fromDetails")
        fillLotteryForm(lotteryName)
        submitLotteryForm()

        // back on the details screen, which now shows a lottery status
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithText(
                    "Lottery Status:",
                    substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // and the home screen lists the lottery
        goBack()
        waitForLotteryCards(1)
        val lotteryId = waitForLotteryId(lotteryName)
        assertEquals(lotteryId, campsiteLotteryId(campsite.id))
    }

    // ================= NEW: editing a campsite =================

    /** Editing a campsite changes what the details screen, the home list and Firestore show. */
    @Test
    fun editingACampsiteChangesItsDetailsEverywhere() {
        val campsite = seedOwnedCampsite("E")
        launchApp()
        openOwnerHome()
        waitForText(campsite.name)

        val newName = "Edited $runTag"
        val newLocation = "Moved to Jasper $runTag"
        val newDescription = "Edited description $runTag"
        val newMaxOccupancy = 12

        composeTestRule
            .onNodeWithText(campsite.name)
            .performClick()   // card -> details

        composeTestRule
            .onNodeWithText("Update Campsite")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithTag("campsiteNameInput")
            .performTextReplacement(newName)

        composeTestRule
            .onNodeWithTag("campsiteLocationInput")
            .performTextReplacement(newLocation)

        composeTestRule
            .onNodeWithTag("campsiteMaximumOccupantInput")
            .performTextReplacement(newMaxOccupancy.toString())

        composeTestRule
            .onNodeWithTag("campsiteDescriptionInput")
            .performTextReplacement(newDescription)

        composeTestRule
            .onNodeWithText("update campsite")
            .performScrollTo()
            .performClick()

        // back on the details screen: new values, old name gone
        waitForText(newName)
        composeTestRule
            .onNodeWithText(newName)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(newLocation, substring = true)
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Max Occupancy: $newMaxOccupancy", substring = true)
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(newDescription)
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(campsite.name)
            .assertDoesNotExist()

        // and so does the database
        waitUntilTrue("Firestore to have the edited details") {
            val doc = Tasks.await(
                firestore.collection("campsites").document(campsite.id).get(), 10, TimeUnit.SECONDS
            )
            doc.getString("name") == newName &&
                    doc.getString("location") == newLocation &&
                    doc.getString("description") == newDescription &&
                    doc.getLong("maxOccupancy")?.toInt() == newMaxOccupancy
        }

        // the home list shows the new values too
        goBack()
        waitForText(newName)
        composeTestRule
            .onNodeWithText(newName)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(newLocation)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("$newMaxOccupancy people max")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(campsite.name)
            .assertDoesNotExist()
    }

    // ================= NEW: a lottery that runs its course =================

    /**
     * A short lottery (draw in about 1.5 minutes, booking closes about a minute after) runs its course in the database.
     * Slow (3 to 8 minutes) and it depends on whatever runs the draw. Run it on its own, not with the rest.
     */
    @Test
    fun shortLotteryRunsItsCourse() {
        val campsite = seedOwnedCampsite("S")
        launchApp()
        openOwnerHome()
        waitForText(campsite.name)

        val lotteryName = newLotteryName("short")
        createLotteryFromHome(
            lotteryName,
            listOf(campsite),
            drawInMillis = 90_000L,
            bookingClosesInMillis = 150_000L
        )
        waitForLotteryCards(1)
        val lotteryId = waitForLotteryId(lotteryName)
        waitForLotteryStatus(lotteryId, "initialAccepting", timeoutMs = 30_000L)

        // the draw time passes: the status has to move on to "booking", and later to "complete"
        waitForLotteryStatus(lotteryId, "booking", timeoutMs = 4 * 60_000L)
        waitForLotteryStatus(lotteryId, "complete", timeoutMs = 4 * 60_000L)

        // the owner sees the finished status on the campsite's details screen
        composeTestRule
            .onNodeWithText(campsite.name)
            .performClick()
        waitForText("complete")

        // TODO: what should "disappears" mean once the lottery has finished? (card gone, campsite freed, ...)
    }

    @Test
    fun tappingLotteryCardShowsDetailsThenPrefilledEditForm() {
        val campsite = seedOwnedCampsite("L")
        launchApp()
        openOwnerHome()
        waitForText(campsite.name)

        // Create a lottery to test with
        val lotteryName = newLotteryName("toEdit")
        createLotteryFromHome(lotteryName, listOf(campsite))
        waitForLotteryCards(1)

        // 1. Tap the lottery card on the home screen
        composeTestRule
            .onNodeWithText(lotteryName, substring = true)
            .performScrollTo()
            .performClick()

        // 2. Verify we made it to the Lottery Details Screen
        composeTestRule
            .onNodeWithText("Edit lottery")
            .performScrollTo()
            .assertIsDisplayed()

        // 3. Click "Edit lottery"
        composeTestRule
            .onNodeWithText("Edit lottery")
            .performClick()

        // 4. Verify we are on the Edit Form and the name is prefilled!
        composeTestRule
            .onNodeWithTag("lotteryName")
            .assertTextContains(lotteryName)

        // 5. Verify the campsite is pre-selected in the list
        composeTestRule
            .onNodeWithText(campsite.name)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /** Using the "View Lotteries" option in the campsite card's dropdown opens the lottery details. */
    @Test
    fun viewLotteriesFromCampsiteCardDropdownOpensLotteryDetails() {
        val campsite = seedOwnedCampsite("M")
        launchApp()
        openOwnerHome()
        waitForText(campsite.name)

        // Create a lottery so the campsite is attached to one
        val lotteryName = newLotteryName("dropdownTest")
        createLotteryFromHome(lotteryName, listOf(campsite))
        waitForLotteryCards(1)

        // 1. Open the 3-dot menu on the campsite card
        composeTestRule
            .onNodeWithContentDescription("Options Menu")
            .performScrollTo()
            .performClick()

        // 2. Click "View Lotteries"
        composeTestRule
            .onNodeWithText("View Lotteries")
            .performClick()

        // 3. Verify it routed us correctly to the Lottery Details Screen
        composeTestRule
            .onNodeWithText(lotteryName, substring = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Edit lottery")
            .performScrollTo()
            .assertIsDisplayed()
    }

    /** Editing a lottery changes its details on the home list. */
    @Test
    fun editingALotteryChangesItsDetailsEverywhere() {
        val campsite = seedOwnedCampsite("E")
        launchApp()
        openOwnerHome()
        waitForText(campsite.name)

        // 1. Create the initial lottery to test with
        val originalName = newLotteryName("toEdit")
        createLotteryFromHome(originalName, listOf(campsite))
        waitForLotteryCards(1)

        // 2. Open the lottery details and tap Edit
        composeTestRule
            .onNodeWithText(originalName, substring = true, useUnmergedTree = true)
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithText("Edit lottery")
            .performScrollTo()
            .performClick()

        // 3. Edit the lottery details
        val newName = "Edited Lottery $runTag"
        val newWinners = 5

        composeTestRule
            .onNodeWithTag("lotteryName")
            .performScrollTo()
            .performTextReplacement(newName)

        composeTestRule
            .onNodeWithTag("lotteryWinners")
            .performScrollTo()
            .performTextReplacement(newWinners.toString())

        // 4. Save the changes
        composeTestRule
            .onNodeWithTag("createLotteryButton")
            .performScrollTo()
            .performClick()

        // Wait for the edit form to close
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithTag("createLotteryButton")
                .fetchSemanticsNodes()
                .isEmpty()
        }

        Thread.sleep(2000)

        // 5. INSTEAD OF goBack(), explicitly tap the Home Tab to force a refresh
        composeTestRule
            .onNodeWithTag("tab_campsiteshome")
            .performClick()

        // Wait for the loading spinner to vanish
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithTag("ownerLoading")
                .fetchSemanticsNodes()
                .isEmpty()
        }

        // 6. Verify the Home Screen Card updated to the new name!
        // INSTEAD OF waitForText(newName), we use a smart wait that allows substrings:
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithText(newName, substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithText(newName, substring = true, useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()

        // Ensure the old name is completely gone from the list
        composeTestRule
            .onAllNodesWithText(originalName, substring = true, useUnmergedTree = true)
            .assertCountEquals(0)
    }






    // ---------- helpers ----------

    private fun ownerRef() = firestore
        .collection("ownerData")
        .document(deviceId)

    private fun launchApp() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    /** The phone's back action. Used instead of tapping "back button", because the photo carousel has one too. */
    private fun goBack() {
        scenario?.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeTestRule.waitForIdle()
    }

    /** Taps the owner tab, then waits for the screen to appear and its loading spinner to go away. */
    private fun openOwnerHome() {
        composeTestRule
            .waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag("tab_campsiteshome")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onNodeWithTag("tab_campsiteshome")
            .performClick()

        composeTestRule
            .waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithTag("ownerHomeScreen")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithTag("ownerLoading")
                .fetchSemanticsNodes()
                .isEmpty()
        }
    }

    private fun waitForText(text: String) {
        composeTestRule
            .waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithText(text)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun waitForLotteryCards(count: Int) {
        composeTestRule
            .waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithTag("lotteryCard")
                .fetchSemanticsNodes()
                .size == count
        }
    }

    private fun openAddLotteryFromHome() {
        composeTestRule
            .onNodeWithTag("ownerThreeDotsMenu")
            .performClick()
        composeTestRule
            .onNodeWithTag("addLotteryMenuItem")
            .performClick()
    }

    // ----- lottery form -----

    private fun newLotteryName(label: String) = "Lottery $label $runTag"

    /** Types a name, the draw time, the booking-close time (both counted from now) and the winners into the lottery form. */
    private fun fillLotteryForm(
        name: String,
        drawInMillis: Long = oneDayMillis,
        bookingClosesInMillis: Long = 2 * oneDayMillis,
        winners: Int = 1
    ) {
        createdLotteryNames.add(name)   // registered first, so cleanup deletes it even if the test fails
        typeInto("lotteryName", name)
        typeDate("lotteryStart", drawInMillis)
        typeDate("lotteryFinal", bookingClosesInMillis)
        typeInto("lotteryWinners", winners.toString())
    }

    /** Opens the "Add a campsite..." dropdown in the lottery form and picks the campsite. */
    private fun pickCampsiteInLotteryForm(campsiteName: String) {
        composeTestRule
            .onNodeWithText("Add a campsite...")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithText(campsiteName)
            .performClick()
    }

    /** Taps "Create Lottery" and waits until the form has closed. */
    private fun submitLotteryForm() {
        composeTestRule
            .onNodeWithTag("createLotteryButton")
            .performScrollTo()
            .performClick()

        try {
            composeTestRule.waitUntil(timeoutMillis = 15_000) {
                composeTestRule
                    .onAllNodesWithTag("createLotteryButton")
                    .fetchSemanticsNodes()
                    .isEmpty()
            }
        } catch (e: Throwable) {
            throw AssertionError(
                "The lottery form never closed. It is probably showing a validation error (red text near the bottom of the form).",
                e
            )
        }
    }

    private fun createLotteryFromHome(
        name: String,
        campsites: List<SeededCampsite>,
        drawInMillis: Long = oneDayMillis,
        bookingClosesInMillis: Long = 2 * oneDayMillis
    ) {
        openAddLotteryFromHome()
        campsites
            .forEach {
                pickCampsiteInLotteryForm(it.name)
            }
        fillLotteryForm(name, drawInMillis, bookingClosesInMillis)
        submitLotteryForm()
    }

    private fun typeInto(tag: String, text: String) {
        composeTestRule
            .onNodeWithTag(tag)
            .performScrollTo()
            .performTextInput(text)
    }

    private class DateFields(val year: Int, val month: Int, val day: Int, val hour: Int, val minute: Int)

    private fun edmontonFields(millis: Long): DateFields {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("America/Edmonton"))
        cal.timeInMillis = millis
        return DateFields(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE)
        )
    }

    /**
     * Types a moment (counted from now) into the five date fields starting with `prefix`.
     * dateConverterToLong takes an hour off the typed time, so this asks the converter what a typed time turns into
     * and corrects for it. That way the lottery really ends when the test intends, whether or not that hack is still in the app.
     */
    private fun typeDate(prefix: String, fromNowMillis: Long) {
        val target = System.currentTimeMillis() + fromNowMillis
        val guess = edmontonFields(target)
        val appResult = dateConverterToLong()
            .convertDateToLong(guess.year, guess.month, guess.day, guess.hour, guess.minute)
        val shift = (target - target % 60_000L) - appResult   // 3_600_000 while the app takes an hour off, else 0
        val typed = edmontonFields(target + shift)
        typeInto("${prefix}Day", typed.day.toString())
        typeInto("${prefix}Month", typed.month.toString())
        typeInto("${prefix}Year", typed.year.toString())
        typeInto("${prefix}Hour", typed.hour.toString())
        typeInto("${prefix}Minute", typed.minute.toString())
    }

    // ----- Firestore -----

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

    private fun waitUntilTrue(what: String, timeoutMs: Long = 15_000L, condition: () -> Boolean) {
        val end = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < end) {
            if (condition()) return
            Thread.sleep(300)
        }
        fail("Timed out waiting for $what")
    }

    /** The id of the lottery with this name, once Firestore has it. */
    private fun waitForLotteryId(name: String): String {
        var found: String? = null
        waitUntilTrue("the lottery \"$name\" to exist in Firestore") {
            found = Tasks.await(
                firestore.collection("lotteries").whereEqualTo("name", name).get(),
                10, TimeUnit.SECONDS
            ).documents.firstOrNull()?.id
            found != null
        }
        return found!!
    }

    private fun campsiteLotteryId(campsiteId: String): String? =
        Tasks.await(firestore.collection("campsites").document(campsiteId).get(), 10, TimeUnit.SECONDS)
            .getString("lotteryId")

    private fun lotteryStatus(lotteryId: String): String? =
        Tasks.await(firestore.collection("lotteries").document(lotteryId).get(), 10, TimeUnit.SECONDS)
            .getString("status")

    private fun waitForLotteryStatus(lotteryId: String, expected: String, timeoutMs: Long) {
        val end = System.currentTimeMillis() + timeoutMs
        var last: String? = null
        while (System.currentTimeMillis() < end) {
            last = lotteryStatus(lotteryId)
            if (last == expected) return
            Thread.sleep(2_000)
        }
        fail(
            "Lottery $lotteryId never reached status \"$expected\" within ${timeoutMs / 1000}s (last status: $last). " +
                    "Is the function that runs the draw deployed and running?"
        )
    }
}