package com.example.syntax_camping

import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import com.example.syntax_camping.CampsiteRepository
import android.net.Uri
import com.google.firebase.firestore.FieldPath.documentId
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.lotteryRepository
import com.google.firebase.Timestamp
import java.sql.Timestamp as jsqlTimestamp
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.assertFalse
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import com.example.syntax_camping.dataClasses.LotteryEntry
import org.junit.Assert.*
class LotteryTesting {
    private lateinit var testCampsite: Campsite
    private lateinit var db: Database
    private lateinit var testLottery: Lottery
    private lateinit var lr: lotteryRepository


    // Set up a new wizard before every single test
    @Before
    fun setUp() {

        lr = lotteryRepository()
        db = Database()
        val documentID = FirebaseFirestore.getInstance()
            .collection("campsites")
            .document()
            .id
        val mutableTags = mutableListOf<String>("yep", "tags", "you wish you had tags this cool")
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
            mutableListOf<Campsite>(testCampsite),
            2,
            "name",
            "initialBooking"
        )


    }

    @Test
    fun testLotteryCreationInDbAndLotteryRepsoitorygetLottery() {
        testLottery.createLotteryTimer()
        lr.getLottery(testLottery.lotteryId) { lottery ->
            if (lottery == null) {
                throw AssertionError("Lottery was null")
            }


            assert((lottery.endTimeLottery == testLottery.endTimeLottery) && (lottery.endTimeBooking == testLottery.endTimeBooking) && (lottery.name == testLottery.name) && (lottery.numWinners == testLottery.numWinners) && (lottery.capacity == testLottery.capacity))
        }


        //cleanup
        FirebaseFirestore.getInstance().collection("lotteries")
            .document(testLottery.lotteryId)
            .delete()
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->
                // Handle any potential deletion failure if needed
            }
    }

    @Test
    fun testLotteryUpdateAndMulitLottery() {
        testLottery.createLotteryTimer()
        val campsiteList = mutableListOf<Campsite>(
            testLottery.campsites[0],
            Campsite(
                "docId",
                "campnamene",
                "campLocation2",
                "descriptive description2",
                900,
                mutableListOf<String>("wabbafett"),
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
            "initialBooking"
        )

        lr.getLottery(testLottery.lotteryId) { lottery ->
            if (lottery == null) {
                throw AssertionError("Lottery was null")
            }


            assert((lottery.endTimeLottery == testLottery.endTimeLottery + 10000) && (lottery.endTimeBooking == testLottery.endTimeBooking + 200000) && (lottery.name == "newName") && (lottery.numWinners == 1) && (lottery.capacity == 3))
        }

        //cleanup
        FirebaseFirestore.getInstance().collection("lotteries")
            .document(testLottery.lotteryId)
            .delete()
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->
                // Handle any potential deletion failure if needed
            }
    }

    @Test
    fun testLotteryJoin() {

        testLottery.createLotteryTimer()
        val littleLotteryGuy = LotteryEntry("madeUpUserId", Timestamp.now())
        testLottery.addEntry(littleLotteryGuy) { success ->
            lr.getEntries(testLottery.lotteryId) { entries ->
                if (entries == null) {
                    throw AssertionError("entries was null")
                }


                assert(entries == mutableListOf<LotteryEntry>(littleLotteryGuy))
            }
            FirebaseFirestore.getInstance().collection("lotteries")
                .document(testLottery.lotteryId)
                .delete()
                .addOnSuccessListener {

                }
                .addOnFailureListener { e ->
                    // Handle any potential deletion failure if needed
                }
        }
    }

    @Test
    fun testGetReadables() {


        val readableLottery = testLottery.getReadableLotteryEndTime()
        val readableBooking = testLottery.getReadableBookingEndTime()
        assert((readableLottery is jsqlTimestamp) && (readableBooking is jsqlTimestamp))
    }

    @Test
    fun testLotteryInThePast() {
        testLottery = Lottery(
            22400000,
            22600000,
            2,
            mutableListOf<Campsite>(testCampsite),
            2,
            "name",
            "initialBooking"
        )
        val worked = testLottery.createLotteryTimer()

        assert(worked == false)



    }


    @Test
    fun testLotteryJoinAtCapacity() {

        testLottery.createLotteryTimer()
        val littleLotteryGuy = LotteryEntry("madeUpUserId", Timestamp.now())
        val littleLotteryGuy2 = LotteryEntry("madeupuserId2", Timestamp.now())
        val littleLotteryGuy3 = LotteryEntry("madeUpUserId3", Timestamp.now())
        testLottery.addEntry(littleLotteryGuy) { success1 ->
            assertTrue("First entry should succeed", success1)
            testLottery.addEntry(littleLotteryGuy2) { success2 ->
                assertTrue("second entry should succeed", success2)
                testLottery.addEntry(littleLotteryGuy3) { success3 ->
                    assertFalse("third entry fails", success3)

            FirebaseFirestore.getInstance().collection("lotteries")
                .document(testLottery.lotteryId)
                .delete()
                .addOnSuccessListener {

                }
                .addOnFailureListener { e ->
                    // Handle any potential deletion failure if needed
                }
                }
        }
    }
    }
    @Test
    fun testLotteryJoinAtalreadyatjoined() {

        testLottery.createLotteryTimer()
        val littleLotteryGuy = LotteryEntry("madeUpUserId", Timestamp.now())

        testLottery.addEntry(littleLotteryGuy) { success1 ->
            assertTrue("First entry should succeed", success1)
            testLottery.addEntry(littleLotteryGuy) { success2 ->
                assertFalse("second entry should succeed", success2)


                    FirebaseFirestore.getInstance().collection("lotteries")
                        .document(testLottery.lotteryId)
                        .delete()
                        .addOnSuccessListener {

                        }
                        .addOnFailureListener { e ->
                            // Handle any potential deletion failure if needed
                        }

            }
        }
    }



    @Test
    fun testLotteryRemoval() {
        testLottery.createLotteryTimer()
        val littleLotteryGuy = LotteryEntry("madeUpUserId", Timestamp.now())
        val latch = CountDownLatch(1)

        testLottery.addEntry(littleLotteryGuy) { success ->
            assertTrue("Failed to add entry", success)

            lr.getEntries(testLottery.lotteryId) { entries ->
                assertNotNull("Entries was null", entries)
                assertEquals(1, entries?.size)

                // 1. Call removeEntry (no callback)
                testLottery.removeEntry(littleLotteryGuy)

                // 2. Wait 2 seconds for Firebase to sync the deletion in the background,
                // then fetch and assert.
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    lr.getEntries(testLottery.lotteryId) { updatedEntries ->
                        assertNotNull("Updated entries was null", updatedEntries)
                        assertTrue("Entry was not removed from Firebase", updatedEntries.isNullOrEmpty())

                        // 3. Cleanup and finish test (UNCOMMENTED SO LATCH COUNTS DOWN!)
                        FirebaseFirestore.getInstance().collection("lotteries")
                            .document(testLottery.lotteryId)
                            .delete()
                            .addOnCompleteListener {
                                latch.countDown() // <-- This releases the test thread!
                            }
                    }
                }, 2000) // 2-second delay is plenty of time for Firebase sync
            }
        }

        // Give it a slightly longer timeout than the delay (e.g., 10 seconds)
        val finished = latch.await(10, TimeUnit.SECONDS)
        assertTrue("Test timed out", finished)
    }
}