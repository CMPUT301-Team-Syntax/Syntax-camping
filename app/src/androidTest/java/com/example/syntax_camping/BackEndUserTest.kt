package com.example.syntax_camping
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.syntax_camping.dataClasses.EntrantData
import com.example.syntax_camping.dataClasses.OwnerData
import com.example.syntax_camping.dataClasses.User
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

@RunWith(AndroidJUnit4::class)
class BackEndUserTest {
    private val db = Database()

    //Check if get device ID works
    @Test
    fun checkDeviceID() {
        val context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        val deviceId = db.getDeviceId(context)

        assertFalse(deviceId.isBlank())
    }

    //Check if create user works.
    @Test
    fun didWeCreateAUser() {
        val testID = "test_${UUID.randomUUID()}"

        val testUser = User(
            userId = testID,
            name = "Test user",
            email = "test@example.com",
            phoneNumber = "1234567890",
            entrantData = EntrantData(
                reservationIds = mutableListOf("reservation1"),
                lotteryEntryIds = mutableListOf("entry1")
            ),
            ownerData = OwnerData(
                lotteryIds = mutableListOf("lottery1"),
                campsiteIds = mutableListOf("campsite1")
            )
        )

        val latch = CountDownLatch(1)
        var result: Result<Unit>? = null

        db.createNewUser(testID, testUser) {callbackResult ->
            result = callbackResult
            latch.countDown()
        }

        assertTrue(
            "Timed out waiting for Firestore",
            latch.await(15, TimeUnit.SECONDS)
        )

        assertTrue(
            "Failed to create user: ${result?.exceptionOrNull()?.message}",
            result?.isSuccess == true
        )
    }

    //Check if fetchUser works.
    @Test
    fun fetchUserTest() {
        //Repeat of create user test (to actually initialize test user).
        val testId = "test_${UUID.randomUUID()}"
        val originalUser = User(
            userId = testId,
            name = "Test User",
            email = "test@example.com",
            phoneNumber = "1234567890",
            entrantData = EntrantData(
                reservationIds = mutableListOf("reservation1"),
                lotteryEntryIds = mutableListOf("entry1")
            ),
            ownerData = OwnerData(
                lotteryIds = mutableListOf("lottery1"),
                campsiteIds = mutableListOf("campsite1")
            )
        )

        // Create the user first.
        val createLatch = CountDownLatch(1)
        var createResult: Result<Unit>? = null

        db.createNewUser(testId, originalUser) { result ->
            createResult = result
            createLatch.countDown()
        }

        assertTrue(   //creating a test user took too long and failed.
            "Timed out creating test user",
            createLatch.await(15, TimeUnit.SECONDS)
        )
        assertTrue(   //creating a user failed.
            "Failed to create test user: ${createResult?.exceptionOrNull()?.message}",
            createResult?.isSuccess == true
        )

        //Fetch the user from Firestore.
        val fetchLatch = CountDownLatch(1)
        var fetchResult: Result<User?>? = null

        db.fetchUserDetails(testId) { result ->
            fetchResult = result
            fetchLatch.countDown()
        }

        assertTrue(  //If its taking too long to fetch user, it fails.
            "Timed out fetching test user",
            fetchLatch.await(15, TimeUnit.SECONDS)
        )
        assertTrue(   //If user itself can't be fetched, it fails.
            "Failed to fetch user: ${fetchResult?.exceptionOrNull()?.message}",
            fetchResult?.isSuccess == true
        )

        val fetchedUser = fetchResult?.getOrNull()
        assertNotNull("Fetched user should not be null", fetchedUser)

        //Verify user's fields.
        assertEquals(originalUser.userId, fetchedUser?.userId)
        assertEquals(originalUser.name, fetchedUser?.name)
        assertEquals(originalUser.email, fetchedUser?.email)
        assertEquals(originalUser.phoneNumber, fetchedUser?.phoneNumber)

        //Verify entrant data (reservationIds).
        assertEquals(
            originalUser.entrantData?.reservationIds,
            fetchedUser?.entrantData?.reservationIds
        )
        //Verify entrant data (lotteryEntryIds).
        assertEquals(
            originalUser.entrantData?.lotteryEntryIds,
            fetchedUser?.entrantData?.lotteryEntryIds
        )

        //Verify owner data (lotteryIds).
        assertEquals(
            originalUser.ownerData?.lotteryIds,
            fetchedUser?.ownerData?.lotteryIds
        )
        //Verify owner data (campsiteIds).
        assertEquals(
            originalUser.ownerData?.campsiteIds,
            fetchedUser?.ownerData?.campsiteIds
        )
    }

    //Check if updateUser works.
    @Test
    fun checkIfUserIsUpdated() {
        //Create a new user
        val testId = "test_${UUID.randomUUID()}"

        val originalUser = User(
            userId = testId,
            name = "Original Name",
            email = "original@example.com",
            phoneNumber = "1111111111",
            entrantData = EntrantData(
                reservationIds = mutableListOf("reservation1"),
                lotteryEntryIds = mutableListOf("entry1")
            ),
            ownerData = OwnerData(
                lotteryIds = mutableListOf("lottery1"),
                campsiteIds = mutableListOf("campsite1")
            )
        )

        //Tests if the user creation had any fails.
        val createLatch = CountDownLatch(1)
        var createResult: Result<Unit>? = null

        db.createNewUser(testId, originalUser) { result ->
            createResult = result
            createLatch.countDown()
        }

        assertTrue(   //Failed to connect to Firestore.
            "Timed out creating user",
            createLatch.await(15, TimeUnit.SECONDS)
        )
        assertTrue(   //Failed to create user.
            "Failed to create user: ${createResult?.exceptionOrNull()?.message}",
            createResult?.isSuccess == true
        )

        //Creates an updated copy of the original test user.
        val updatedUser = originalUser.copy(
            name = "Updated Name",
            email = "updated@example.com",
            phoneNumber = "2222222222",
            entrantData = EntrantData(
                reservationIds = mutableListOf("reservation2"),
                lotteryEntryIds = mutableListOf("entry2")
            ),
            ownerData = OwnerData(
                lotteryIds = mutableListOf("lottery2"),
                campsiteIds = mutableListOf("campsite2")
            )
        )

        //Updates the user with test cases if it failed or not.
        val updateLatch = CountDownLatch(1)
        var updateResult: Result<Unit>? = null

        db.updateUser(testId, updatedUser) {result ->
            updateResult = result
            updateLatch.countDown()
        }

        assertTrue(
            "Timed out updating user",
            updateLatch.await(15, TimeUnit.SECONDS)
        )
        assertTrue(
            "Failed to update user: ${updateResult?.exceptionOrNull()?.message}",
            updateResult?.isSuccess == true
        )

        //Fetches user and tests if every data is updated or not.
        val fetchLatch = CountDownLatch(1)
        var fetchResult: Result<User?>? = null

        db.fetchUserDetails(testId) {result ->
            fetchResult = result
            fetchLatch.countDown()
        }

        assertTrue(
            "Timed out while fetching updated user.",
            fetchLatch.await(15, TimeUnit.SECONDS)
        )
        assertTrue(
            "Failed to fetch updated user: ${fetchResult?.exceptionOrNull()?.message}",
            fetchResult?.isSuccess == true
        )

        val fetchedUser = fetchResult?.getOrNull()

        // Verify entrant data.
        assertEquals(
            updatedUser.entrantData?.reservationIds,
            fetchedUser?.entrantData?.reservationIds
        )
        assertEquals(
            updatedUser.entrantData?.lotteryEntryIds,
            fetchedUser?.entrantData?.lotteryEntryIds
        )

        // Verify owner data.
        assertEquals(
            updatedUser.ownerData?.lotteryIds,
            fetchedUser?.ownerData?.lotteryIds
        )
        assertEquals(
            updatedUser.ownerData?.campsiteIds,
            fetchedUser?.ownerData?.campsiteIds
        )
    }

    //Check if userExistsByDeviceId works.
    @Test
    fun UserExistByDeviceIdWorks() {
        val existingId = "test_${UUID.randomUUID()}"
        val missingId = "missing_${UUID.randomUUID()}"

        val testUser = User(
            userId = existingId,
            name = "Test User",
            email = "test@example.com",
            phoneNumber = "1234567890"
        )

        //Create a test user
        val createLatch = CountDownLatch(1)
        var createResult: Result<Unit>? = null

        db.createNewUser(existingId, testUser) { result ->
            createResult = result
            createLatch.countDown()
        }

        assertTrue(
            "Timed out creating test user",
            createLatch.await(15, TimeUnit.SECONDS)
        )

        assertTrue(
            "Failed to create test user: ${createResult?.exceptionOrNull()?.message}",
            createResult?.isSuccess == true
        )
        // STEP 2: Check that the existing user returns true.
        val existingLatch = CountDownLatch(1)
        var existingResult = false

        db.userExistsByDeviceId(existingId) { exists ->
            existingResult = exists
            existingLatch.countDown()
        }

        assertTrue(
            "Timed out checking existing user",
            existingLatch.await(15, TimeUnit.SECONDS)
        )

        assertTrue(
            "Existing user should return true",
            existingResult
        )

       //Check if a non-existant user returns false.
        val missingLatch = CountDownLatch(1)
        var missingResult = true

        db.userExistsByDeviceId(missingId) { exists ->
            missingResult = exists
            missingLatch.countDown()
        }

        assertTrue(
            "Timed out checking nonexistent user",
            missingLatch.await(15, TimeUnit.SECONDS)
        )

        assertFalse(
            "Nonexistent user should return false",
            missingResult
        )
    }

}