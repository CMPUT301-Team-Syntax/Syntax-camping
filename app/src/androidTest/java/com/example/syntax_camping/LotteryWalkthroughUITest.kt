package com.example.syntax_camping

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class LotteryWalkthroughUITest {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private val db = Database()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var deviceId: String
    private var originalOwnerData: Map<String, Any>? = null
    private var originalEntrantData: Map<String, Any>? = null
    private var scenario: ActivityScenario<MainActivity>? = null

    private val runTag = UUID.randomUUID().toString().take(8)
    private val oneDayMillis = 24L * 60 * 60 * 1000
    private val seededCampsiteIds = mutableListOf<String>()
    private val seededLotteryIds = mutableListOf<String>()

    private val emptyOwner = mapOf(
        "lotteryIds" to emptyList<String>(),
        "ownerCampsiteIds" to emptyList<String>()
    )
    private val emptyEntrant = mapOf(
        "entrantReservationIds" to emptyList<String>(),
        "entrantLotteryEntryIds" to emptyList<String>()
    )

    private class SeededCampsite(val id: String, val name: String, val location: String)

    @Before
    fun setUpCleanUserState() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        deviceId = db.getDeviceId(context)

        val userRef = firestore.collection("users").document(deviceId)
        if (!Tasks.await(userRef.get(), 15, TimeUnit.SECONDS).exists()) {
            Tasks.await(
                userRef.set(
                    mapOf(
                        "displayName" to "Test Entrant",
                        "displayEmail" to "entrant@example.com",
                        "displayPhoneNumber" to "0000000000"
                    )
                ),
                15, TimeUnit.SECONDS
            )
        }

        originalOwnerData = Tasks.await(ownerRef().get(), 15, TimeUnit.SECONDS).data
        originalEntrantData = Tasks.await(entrantRef().get(), 15, TimeUnit.SECONDS).data

        Tasks.await(ownerRef().set(emptyOwner), 15, TimeUnit.SECONDS)
        Tasks.await(entrantRef().set(emptyEntrant), 15, TimeUnit.SECONDS)
    }

    @After
    fun tearDown() {
        scenario?.close()

        tryCleanup {
            for (id in seededCampsiteIds) {
                Tasks.await(firestore.collection("campsites").document(id).delete(), 15, TimeUnit.SECONDS)
            }
        }
        tryCleanup {
            for (lotId in seededLotteryIds) {
                val lotRef = firestore.collection("lotteries").document(lotId)
                // Clean subcollections if any entries were created
                for (sub in listOf("entries", "waitlist", "winners")) {
                    val docs = Tasks.await(lotRef.collection(sub).get(), 10, TimeUnit.SECONDS)
                    for (doc in docs.documents) {
                        Tasks.await(doc.reference.delete(), 10, TimeUnit.SECONDS)
                    }
                }
                Tasks.await(lotRef.delete(), 15, TimeUnit.SECONDS)
            }
        }
        tryCleanup {
            Tasks.await(ownerRef().set(originalOwnerData ?: emptyOwner), 15, TimeUnit.SECONDS)
        }
        tryCleanup {
            Tasks.await(entrantRef().set(originalEntrantData ?: emptyEntrant), 15, TimeUnit.SECONDS)
        }
    }

    private fun tryCleanup(step: () -> Unit) {
        try {
            step()
        } catch (e: Throwable) {
            android.util.Log.e("CLEANUP", "Cleanup step failed", e)
        }
    }

    // ==================== TESTS ====================

    /** 1. A campsite with no lottery attached shows "No lotteries available" and no join button. */
    @Test
    fun searchCampsiteWithNoLotteryShowsNoLotteriesAvailable() {
        val camp = seedCampsite("NoLot", ownedByCurrentUser = false)
        launchApp()
        searchAndOpenCampsite(camp.name)

        composeTestRule
            .onNodeWithText("No lotteries available")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("joinLotteryButton")
            .assertDoesNotExist()
    }

    /**
     * 2. Entrant joins an open lottery ("initialAccepting"), sees "Join Successful" popup,
     * count updates to 1, state persists when navigating back and returning, and leaving
     * shows "Leave Successful" and resets count to 0.
     */
    @Test
    fun entrantCanJoinAndLeaveOpenLotteryWithPopupsCountUpdatesAndPersistence() {
        val camp = seedCampsite("OpenLot", ownedByCurrentUser = false)
        seedLotteryForCampsite(
            campsite = camp,
            name = "Summer Draw $runTag",
            status = "initialAccepting",
            capacity = 5
        )

        launchApp()
        searchAndOpenCampsite(camp.name)

        // Wait for the lottery section to load
        waitForTextSubstring("0 people in lottery")

        composeTestRule
            .onNodeWithText("Click to enter lottery!!")
            .performScrollTo()
            .assertIsDisplayed()

        // Join the lottery
        composeTestRule
            .onNodeWithTag("joinLotteryButton")
            .performScrollTo()
            .performClick()

        // Popup appears saying "Join Successful"
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithTag("lotteryStatusPopup").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onNodeWithText("Join Successful")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("lotteryStatusPopupOk")
            .performClick()

        // Count updates to 1 and button changes to "Leave Lottery"
        waitForTextSubstring("1 people in lottery")
        composeTestRule
            .onNodeWithTag("leaveLotteryButton")
            .performScrollTo()
            .assertIsDisplayed()

        // Navigate back to Search and re-open the same campsite to verify persistence
        goBack()
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithText(camp.name, substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithText(camp.name, useUnmergedTree = true)
            .performScrollTo()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithTag("leaveLotteryButton").fetchSemanticsNodes().isNotEmpty()
        }
        waitForTextSubstring("1 people in lottery")

        // Now leave the lottery
        composeTestRule
            .onNodeWithTag("leaveLotteryButton")
            .performScrollTo()
            .performClick()

        // Popup appears saying "Leave Successful"
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithTag("lotteryStatusPopup").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onNodeWithText("Leave Successful")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("lotteryStatusPopupOk")
            .performClick()

        // Count drops back to 0 and Join button returns
        waitForTextSubstring("0 people in lottery")
        composeTestRule
            .onNodeWithTag("joinLotteryButton")
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * 3. When the lottery draw period has ended and status is "booking", an entrant can
     * join the waitlist, see the waitlist count update, and leave the waitlist.
     */
    @Test
    fun entrantCanJoinAndLeaveWaitlistDuringBookingStage() {
        val camp = seedCampsite("WaitlistCamp", ownedByCurrentUser = false)
        seedLotteryForCampsite(
            campsite = camp,
            name = "Waitlist Draw $runTag",
            status = "booking",
            capacity = 5
        )

        launchApp()
        searchAndOpenCampsite(camp.name)

        waitForTextSubstring("0 people on waitlist")

        composeTestRule
            .onNodeWithText("Click to join waitlist!!")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("joinLotteryButton")
            .performScrollTo()
            .performClick()

        // Verify "Join Successful" popup
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithTag("lotteryStatusPopup").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onNodeWithTag("lotteryStatusPopupOk")
            .performClick()

        // Verify waitlist count updates to 1 and button says "Leave Waitlist"
        waitForTextSubstring("1 people on waitlist")
        composeTestRule
            .onNodeWithText("Leave Waitlist")
            .performScrollTo()
            .assertIsDisplayed()

        // Leave waitlist
        composeTestRule
            .onNodeWithTag("leaveLotteryButton")
            .performScrollTo()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithTag("lotteryStatusPopup").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onNodeWithTag("lotteryStatusPopupOk")
            .performClick()

        waitForTextSubstring("0 people on waitlist")
    }

    /** 4. When a lottery is already at capacity, another entrant cannot join it. */
    @Test
    fun entrantCannotJoinLotteryWhenCapacityIsFull() {
        val camp = seedCampsite("FullCamp", ownedByCurrentUser = false)
        val lotteryId = seedLotteryForCampsite(
            campsite = camp,
            name = "CapOne $runTag",
            status = "initialAccepting",
            capacity = 1
        )
        // Seed 1 existing dummy entrant so capacity (1) is already reached
        Tasks.await(
            firestore.collection("lotteries")
                .document(lotteryId)
                .collection("entries")
                .document("other_user_$runTag")
                .set(
                    mapOf(
                        "userId" to "other_user_$runTag",
                        "joinedTimestamp" to Timestamp.now(),
                        "status" to "waiting"
                    )
                ),
            15,
            TimeUnit.SECONDS
        )

        launchApp()
        searchAndOpenCampsite(camp.name)

        waitForTextSubstring("1 people in lottery")

        composeTestRule
            .onNodeWithTag("joinLotteryButton")
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsNotEnabled()

        composeTestRule
            .onNodeWithText("Lottery Full")
            .assertIsDisplayed()
    }

    /** 5. When a lottery's status is "complete", it displays "Lottery ended" and offers no join button. */
    @Test
    fun endedLotteryShowsLotteryEndedAndCannotBeJoined() {
        val camp = seedCampsite("EndedCamp", ownedByCurrentUser = false)
        seedLotteryForCampsite(
            campsite = camp,
            name = "EndedLot $runTag",
            status = "complete",
            capacity = 10
        )

        launchApp()
        searchAndOpenCampsite(camp.name)

        composeTestRule
            .onNodeWithText("Lottery ended")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("joinLotteryButton")
            .assertDoesNotExist()
    }

    /**
     * 6. When an owner views their own campsite from Search:
     * - They cannot rate it or join its lottery.
     * - If it has no lottery, they see "Add Lottery".
     * - If it has an open lottery, they see "Edit Lottery" which opens the prefilled edit screen.
     */
    @Test
    fun ownerViewingOwnCampsiteFromSearchCannotJoinOrRateButCanEditLottery() {
        val ownedWithLottery = seedCampsite("OwnedWithLot", ownedByCurrentUser = true)
        val lotteryName = "OwnerSearchLot $runTag"
        seedLotteryForCampsite(
            campsite = ownedWithLottery,
            name = lotteryName,
            status = "initialAccepting",
            capacity = 10
        )

        launchApp()
        searchAndOpenCampsite(ownedWithLottery.name)

        // Cannot join, sees owner message
        composeTestRule
            .onNodeWithText("You own this campsite. You cannot enter its lottery.")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("joinLotteryButton")
            .assertDoesNotExist()

        // Rating field is hidden for the owner
        composeTestRule
            .onNodeWithTag("leaveRatingButton")
            .assertDoesNotExist()

        // Owner sees "Edit Lottery" underneath the lottery info and clicking it opens the prefilled form
        composeTestRule
            .onNodeWithTag("editLotteryButton")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithTag("lotteryName")
            .assertTextContains(lotteryName)
    }

    /**
     * 7. Reservations empty state shows "No reservations and no lotteries joined.",
     * "Past Reservations" button navigates to PastReservationsScreen, and
     * "Find a lottery to join?" navigates back to Search.
     */
    @Test
    fun reservationsEmptyStateShowsLinksAndPastReservationsButtonWorks() {
        launchApp()
        openReservationsTab()

        composeTestRule
            .onNodeWithText("No reservations and no lotteries joined.")
            .assertIsDisplayed()

        // Test Past Reservations button at the top
        composeTestRule
            .onNodeWithTag("pastReservationsButton")
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithTag("pastReservationsScreen")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("No past reservations found.")
            .assertIsDisplayed()

        // Go back to Reservations and tap "Find a lottery to join?"
        goBack()
        composeTestRule
            .onNodeWithTag("findLotteryToJoinLink")
            .assertIsDisplayed()
            .performClick()

        // Verify we landed on the Search screen
        composeTestRule
            .onNodeWithTag("search_input_field")
            .assertIsDisplayed()
    }

    /**
     * 8. Full Walkthrough: Join a lottery from Search -> Go to Reservations ->
     * See it under "Your Registered Lotteries" with countdown -> Tap it ->
     * See "Leave Lottery" (not Edit) -> Leave it -> Confirm it is removed.
     */
    @Test
    fun joiningLotteryFromSearchShowsInReservationsAndCanBeLeftFromDetails() {
        val camp = seedCampsite("ResFlowCamp", ownedByCurrentUser = false)
        val lotteryName = "ResFlowLot $runTag"
        seedLotteryForCampsite(
            campsite = camp,
            name = lotteryName,
            status = "initialAccepting",
            capacity = 10
        )

        launchApp()
        searchAndOpenCampsite(camp.name)

        // 1. Join the lottery from the campsite details screen
        waitForTextSubstring("0 people in lottery")
        composeTestRule
            .onNodeWithTag("joinLotteryButton")
            .performScrollTo()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithTag("lotteryStatusPopup").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onNodeWithTag("lotteryStatusPopupOk")
            .performClick()

        // 2. Navigate to the Reservations tab
        openReservationsTab()

        composeTestRule
            .onNodeWithText("Your Registered Lotteries")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Your Current Reservations")
            .performScrollTo()
            .assertIsDisplayed()

        // 3. The joined lottery card appears with its name and days left
        composeTestRule
            .onNodeWithText(lotteryName, substring = true, useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        // 4. On LotteryDetailsScreen: shows "Closes in:" countdown and "Leave Lottery" (NOT "Edit lottery")
        waitForTextSubstring("Closes in:")
        composeTestRule
            .onNodeWithText("Edit lottery")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithTag("leaveLotteryButton")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        // 5. Confirm "Leave Successful" popup
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithTag("lotteryStatusPopup").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onNodeWithText("Leave Successful")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("lotteryStatusPopupOk")
            .performClick()

        // 6. Go back to Reservations and verify it returns to the empty state
        goBack()
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithText("No reservations and no lotteries joined.")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    // ==================== HELPERS ====================

    private fun ownerRef() = firestore.collection("ownerData").document(deviceId)
    private fun entrantRef() = firestore.collection("entrantData").document(deviceId)

    private fun launchApp() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    private fun goBack() {
        scenario?.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeTestRule.waitForIdle()
    }

    private fun openReservationsTab() {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag("tab_reservations").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onNodeWithTag("tab_reservations")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithTag("reservationsLoading").fetchSemanticsNodes().isEmpty()
        }
    }

    private fun searchAndOpenCampsite(campsiteName: String) {
        // 1. Wait for the Search bar to appear
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithTag("search_input_field").fetchSemanticsNodes().isNotEmpty()
        }

        // 2. Wait until SearchHomeScreen's LaunchedEffect finishes loading the seeded campsite into CampsiteRepository
        // (We check the repository directly because LazyColumn won't compose the card if it's further down the list!)
        waitUntilTrue("CampsiteRepository to load $campsiteName from Firestore") {
            CampsiteRepository.getCampsites().any { it.name == campsiteName }
        }

        // 3. Type the campsite name into the search bar and click the search icon
        composeTestRule
            .onNodeWithTag("search_input_field")
            .performTextInput(campsiteName)

        composeTestRule
            .onNodeWithTag("search_icon")
            .performClick()

        // 4. Wait for the filtered card to appear at the top of the list, then tap it
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithText(campsiteName, substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithText(campsiteName, useUnmergedTree = true)
            .performClick()
    }

    private fun waitForTextSubstring(substring: String) {
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithText(substring, substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun seedCampsite(label: String, ownedByCurrentUser: Boolean): SeededCampsite {
        val id = "test_camp_${runTag}_${UUID.randomUUID().toString().take(6)}"
        seededCampsiteIds.add(id)
        val campsite = SeededCampsite(id, "Camp $label $runTag", "Location $label")

        Tasks.await(
            firestore.collection("campsites").document(id).set(
                mapOf(
                    "name" to campsite.name,
                    "location" to campsite.location,
                    "description" to "Test campsite description",
                    "maxOccupancy" to 6,
                    "tags" to "Forest,Lake",
                    "images" to "",
                    "lotteryId" to ""
                )
            ),
            15,
            TimeUnit.SECONDS
        )

        if (ownedByCurrentUser) {
            Tasks.await(
                ownerRef().update("ownerCampsiteIds", FieldValue.arrayUnion(id)),
                15,
                TimeUnit.SECONDS
            )
        }
        return campsite
    }

    private fun seedLotteryForCampsite(
        campsite: SeededCampsite,
        name: String,
        status: String,
        capacity: Int?
    ): String {
        val lotRef = firestore.collection("lotteries").document()
        val lotteryId = lotRef.id
        seededLotteryIds.add(lotteryId)

        val now = System.currentTimeMillis()
        val campsiteMap = mapOf(
            "documentId" to campsite.id,
            "name" to campsite.name,
            "location" to campsite.location,
            "description" to "Test campsite description",
            "maxOccupancy" to 6,
            "tags" to "Forest,Lake",
            "rating" to 0.0,
            "imagesAsStrs" to ""
        )

        Tasks.await(
            lotRef.set(
                mapOf(
                    "name" to name,
                    "status" to status,
                    "endTimeLottery" to (now + oneDayMillis),
                    "endTimeBooking" to (now + 2 * oneDayMillis),
                    "numberOfWinners" to 2,
                    "numWinners" to 2,
                    "capacity" to capacity,
                    "campsites" to listOf(campsiteMap)
                )
            ),
            15,
            TimeUnit.SECONDS
        )

        Tasks.await(
            firestore.collection("campsites").document(campsite.id).update("lotteryId", lotteryId),
            15,
            TimeUnit.SECONDS
        )

        return lotteryId
    }

    private fun waitUntilTrue(what: String, timeoutMs: Long = 15_000L, condition: () -> Boolean) {
        val end = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < end) {
            if (condition()) return
            Thread.sleep(300)
        }
        fail("Timed out waiting for $what")
    }
}