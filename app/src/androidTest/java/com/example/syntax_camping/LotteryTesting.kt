package com.example.syntax_camping

import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.LotteryEntry
import com.example.syntax_camping.dataClasses.lotteryRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.sql.Timestamp as jsqlTimestamp
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class LotteryTesting {
    private lateinit var testCampsite: Campsite
    private lateinit var db: Database
    private lateinit var testLottery: Lottery
    private lateinit var lr: lotteryRepository

    @Before
    fun setUp() {
        lr = lotteryRepository()
        db = Database()
        val documentID = FirebaseFirestore.getInstance()
            .collection("campsites")
            .document()
            .id
        val mutableTags = mutableListOf("yep", "tags", "you wish you had tags this cool")
        testCampsite = Campsite(
            documentID,
            "campNametest",
            "campLocation",
            "descriptive description",
            900000000,
            mutableTags,
            0.0
        )
        testLottery = Lottery(
            2051222400000,
            2051222600000,
            2,
            mutableListOf(testCampsite),
            2,
            "name",
            "initialBooking"
        )
    }

    /**
     * Runs assertions safely inside async Firebase callbacks so any AssertionError
     * is captured and rethrown on the main JUnit test thread.
     */

    private fun runAsyncAssert(
        failureRef: AtomicReference<Throwable>,
        latch: CountDownLatch,
        lotteryIdToClean: String? = null,
        block: () -> Unit
    ) {
        try {
            block()
        } catch (t: Throwable) {
            failureRef.compareAndSet(null, t)
        } finally {
            if (lotteryIdToClean != null) {
                FirebaseFirestore.getInstance().collection("lotteries")
                    .document(lotteryIdToClean)
                    .delete()
                    .addOnCompleteListener { latch.countDown() }
            } else {
                latch.countDown()
            }
        }
    }

    private fun awaitAndVerify(latch: CountDownLatch, failureRef: AtomicReference<Throwable>, timeoutSec: Long = 10) {
        val finished = latch.await(timeoutSec, TimeUnit.SECONDS)
        failureRef.get()?.let { throw it }
        assertTrue("Test timed out after $timeoutSec seconds", finished)
    }

    @Test
    fun testLotteryCreationInDbAndLotteryRepsoitorygetLottery() {
        val latch = CountDownLatch(1)
        val failureRef = AtomicReference<Throwable>()

        testLottery.createLotteryTimer()
        lr.getLottery(testLottery.lotteryId) { lottery ->
            runAsyncAssert(failureRef, latch, testLottery.lotteryId) {
                assertNotNull("Lottery was null", lottery)
                assertEquals(testLottery.endTimeLottery, lottery!!.endTimeLottery)
                assertEquals(testLottery.endTimeBooking, lottery.endTimeBooking)
                assertEquals(testLottery.name, lottery.name)
                assertEquals(testLottery.numWinners, lottery.numWinners)
                assertEquals(testLottery.capacity, lottery.capacity)
            }
        }

        awaitAndVerify(latch, failureRef)
    }

    @Test
    fun testLotteryUpdateAndMulitLottery() {
        val latch = CountDownLatch(1)
        val failureRef = AtomicReference<Throwable>()

        testLottery.createLotteryTimer()
        val campsiteList = mutableListOf(
            testLottery.campsites[0],
            Campsite(
                "docId",
                "campnamene",
                "campLocation2",
                "descriptive description2",
                900,
                mutableListOf("wabbafett"),
                1.0
            )
        )

        db.updateDrawTimer(
            testLottery.endTimeLottery + 10000,
            testLottery.endTimeBooking + 200000,
            1,
            campsiteList,
            3,
            "newName",
            testLottery.lotteryId
        )

        // Small delay so async updateDrawTimer write settles before fetching
        Thread.sleep(1000)

        lr.getLottery(testLottery.lotteryId) { lottery ->
            runAsyncAssert(failureRef, latch, testLottery.lotteryId) {
                assertNotNull("Lottery was null", lottery)
                assertEquals(testLottery.endTimeLottery + 10000, lottery!!.endTimeLottery)
                assertEquals(testLottery.endTimeBooking + 200000, lottery.endTimeBooking)
                assertEquals("newName", lottery.name)
                assertEquals(1, lottery.numWinners)
                assertEquals(3, lottery.capacity)
            }
        }

        awaitAndVerify(latch, failureRef)
    }

    @Test
    fun testLotteryJoin() {
        val latch = CountDownLatch(1)
        val failureRef = AtomicReference<Throwable>()

        testLottery.createLotteryTimer()
        val littleLotteryGuy = LotteryEntry("madeUpUserId", Timestamp.now())

        testLottery.addEntry(littleLotteryGuy) { success ->
            if (!success) {
                runAsyncAssert(failureRef, latch, testLottery.lotteryId) {
                    fail("Failed to add entry")
                }
                return@addEntry
            }

            lr.getEntries(testLottery.lotteryId) { entries ->
                runAsyncAssert(failureRef, latch, testLottery.lotteryId) {
                    assertNotNull("Entries was null", entries)
                    assertEquals(mutableListOf(littleLotteryGuy), entries)
                }
            }
        }

        awaitAndVerify(latch, failureRef)
    }

    @Test
    fun testGetReadables() {
        val readableLottery = testLottery.getReadableLotteryEndTime()
        val readableBooking = testLottery.getReadableBookingEndTime()
        assertTrue("Readable lottery end time should be a SQL Timestamp", readableLottery is jsqlTimestamp)
        assertTrue("Readable booking end time should be a SQL Timestamp", readableBooking is jsqlTimestamp)
    }

    @Test
    fun testLotteryInThePast() {
        testLottery = Lottery(
            22400000,
            22600000,
            2,
            mutableListOf(testCampsite),
            2,
            "name",
            "initialBooking"
        )
        val worked = testLottery.createLotteryTimer()
        assertFalse("Lottery in the past should not be created", worked)
    }

    @Test
    fun testLotteryJoinAtCapacity() {
        val latch = CountDownLatch(1)
        val failureRef = AtomicReference<Throwable>()

        testLottery.createLotteryTimer()
        val littleLotteryGuy = LotteryEntry("madeUpUserId", Timestamp.now())
        val littleLotteryGuy2 = LotteryEntry("madeupuserId2", Timestamp.now())
        val littleLotteryGuy3 = LotteryEntry("madeUpUserId3", Timestamp.now())

        testLottery.addEntry(littleLotteryGuy) { success1 ->
            try {
                assertTrue("First entry should succeed", success1)
            } catch (t: Throwable) {
                failureRef.compareAndSet(null, t)
                latch.countDown()
                return@addEntry
            }

            testLottery.addEntry(littleLotteryGuy2) { success2 ->
                try {
                    assertTrue("Second entry should succeed", success2)
                } catch (t: Throwable) {
                    failureRef.compareAndSet(null, t)
                    latch.countDown()
                    return@addEntry
                }

                testLottery.addEntry(littleLotteryGuy3) { success3 ->
                    runAsyncAssert(failureRef, latch, testLottery.lotteryId) {
                        assertFalse("Third entry should fail at capacity", success3)
                    }
                }
            }
        }

        awaitAndVerify(latch, failureRef)
    }

    @Test
    fun testLotteryJoinAtalreadyatjoined() {
        val latch = CountDownLatch(1)
        val failureRef = AtomicReference<Throwable>()

        testLottery.createLotteryTimer()
        val littleLotteryGuy = LotteryEntry("madeUpUserId", Timestamp.now())

        testLottery.addEntry(littleLotteryGuy) { success1 ->
            try {
                assertTrue("First entry should succeed", success1)
            } catch (t: Throwable) {
                failureRef.compareAndSet(null, t)
                latch.countDown()
                return@addEntry
            }

            testLottery.addEntry(littleLotteryGuy) { success2 ->
                runAsyncAssert(failureRef, latch, testLottery.lotteryId) {
                    assertFalse("Duplicate entry should fail", success2)
                }
            }
        }

        awaitAndVerify(latch, failureRef)
    }

    @Test
    fun testLotteryRemoval() {
        testLottery.createLotteryTimer()
        val littleLotteryGuy = LotteryEntry("madeUpUserId", Timestamp.now())
        val latch = CountDownLatch(1)
        val failureRef = AtomicReference<Throwable>()

        testLottery.addEntry(littleLotteryGuy) { success ->
            try {
                assertTrue("Failed to add entry", success)
            } catch (t: Throwable) {
                failureRef.compareAndSet(null, t)
                latch.countDown()
                return@addEntry
            }

            lr.getEntries(testLottery.lotteryId) { entries ->
                try {
                    assertNotNull("Entries was null", entries)
                    assertEquals(1, entries?.size)
                } catch (t: Throwable) {
                    failureRef.compareAndSet(null, t)
                    latch.countDown()
                    return@getEntries
                }

                testLottery.removeEntry(littleLotteryGuy) { removeSuccess ->
                    try {
                        assertTrue("Failed to remove entry", removeSuccess)
                    } catch (t: Throwable) {
                        failureRef.compareAndSet(null, t)
                        latch.countDown()
                        return@removeEntry
                    }

                    lr.getEntries(testLottery.lotteryId) { updatedEntries ->
                        runAsyncAssert(failureRef, latch, testLottery.lotteryId) {
                            assertNotNull("Updated entries was null", updatedEntries)
                            assertTrue(
                                "Entry was not removed from Firebase",
                                updatedEntries.isNullOrEmpty()
                            )
                        }
                    }
                }
            }
        }

        awaitAndVerify(latch, failureRef)
    }

    // Checks if the automatic draw works and puts one person in winners and one in waitlist THis one takes a wminute to run just cause of firebase delays
@Test
fun testLotteryDraw() {
    val latch = CountDownLatch(2) // 1 for winners, 1 for waitlist
    val failureRef = AtomicReference<Throwable>()

    val runSuffix = java.util.UUID.randomUUID().toString().take(8)
    val timeInMillis = System.currentTimeMillis() - 3600000L
    val littleLotteryGuy = LotteryEntry("testUser1_$runSuffix", Timestamp.now())
    val littleLotteryGuy2 = LotteryEntry("testUser2_$runSuffix", Timestamp.now())

    // 30s buffer guarantees addDrawTimer + scheduleLotteryDraw cold start + both addEntry calls
    // all complete well before runLotteryDraw executes
    val drawLotteryId = db.addDrawTimer(
        timeInMillis + 30000,
        timeInMillis + 30000000,
        1,
        mutableListOf(testCampsite),
        null,
        "testingthewaters_$runSuffix"
    )

    Thread.sleep(2000)

    lr.getLottery(drawLotteryId) { lottery ->
        if (lottery == null) {
            failureRef.compareAndSet(null, AssertionError("Lottery was null"))
            repeat(2) { latch.countDown() }
            return@getLottery
        }

        lottery.addEntry(littleLotteryGuy) { success1 ->
            if (!success1) {
                failureRef.compareAndSet(null, AssertionError("First entry should succeed"))
                repeat(2) { latch.countDown() }
                return@addEntry
            }

            lottery.addEntry(littleLotteryGuy2) { success2 ->
                if (!success2) {
                    failureRef.compareAndSet(null, AssertionError("Second entry should succeed"))
                    repeat(2) { latch.countDown() }
                    return@addEntry
                }

                // Poll on a background thread so we NEVER block Android's Main Thread
                Thread {
                    var statusFlipped = false
                    var lastSeenStatus: String? = null
                    for (attempt in 1..180) {
                        Thread.sleep(1000)
                        val checkLatch = CountDownLatch(1)

                        lr.getLottery(drawLotteryId) { updatedLottery ->
                            lastSeenStatus = updatedLottery?.status
                            checkLatch.countDown()
                        }
                        checkLatch.await()

                        if (lastSeenStatus == "booking") {
                            statusFlipped = true
                            break
                        }
                    }

                    if (!statusFlipped) {
                        failureRef.compareAndSet(
                            null,
                            AssertionError("Timed out after 180s waiting for status to change from '$lastSeenStatus' to 'booking'")
                        )
                        repeat(2) { latch.countDown() }
                        return@Thread
                    }

                    // Small buffer in case subcollection writes settle right after status update
                    Thread.sleep(1000)

                    lr.getWinners(drawLotteryId) { updatedWinners ->
                        runAsyncAssert(failureRef, latch) {
                            assertNotNull("Updated winners was null", updatedWinners)
                            assertEquals("Should have 1 winner", 1, updatedWinners!!.size)
                        }
                    }

                    lr.getWaitlist(drawLotteryId) { updatedWaitlist ->
                        runAsyncAssert(failureRef, latch) {
                            assertNotNull("Updated waitlist was null", updatedWaitlist)
                            assertEquals("Should have 1 person in waitlist", 1, updatedWaitlist!!.size)
                        }
                    }
                }.start()
            }
        }
    }

    try {
        awaitAndVerify(latch, failureRef, timeoutSec = 195)
    } finally {
        val docRef = FirebaseFirestore.getInstance().collection("lotteries").document(drawLotteryId)
        docRef.collection("winners").document(littleLotteryGuy.userId).delete()
        docRef.collection("winners").document(littleLotteryGuy2.userId).delete()
        docRef.collection("waitlist").document(littleLotteryGuy.userId).delete()
        docRef.collection("waitlist").document(littleLotteryGuy2.userId).delete()
        docRef.delete()
    }
}

}