//package com.example.syntax_camping
//
//import com.google.firebase.firestore.FirebaseFirestore
//import org.junit.Assert
//import org.junit.Before
//import org.junit.Test
//import com.example.syntax_camping.CampsiteRepository
//import android.net.Uri
//import com.google.firebase.firestore.FieldPath.documentId
//import com.example.syntax_camping.dataClasses.Lottery
//import com.example.syntax_camping.dataClasses.lotteryRepository
//import com.google.firebase.Timestamp
//import java.sql.Timestamp as jsqlTimestamp
//
//
//import com.example.syntax_camping.dataClasses.LotteryEntry
//
//class LotteryTesting {
//    private lateinit var testCampsite: Campsite
//    private lateinit var db: Database
//    private lateinit var testLottery: Lottery
//    private lateinit var lr: lotteryRepository
//
//
//    // Set up a new wizard before every single test
//    @Before
//    fun setUp() {
//
//        lr= lotteryRepository()
//        db = Database()
//        val documentID = FirebaseFirestore.getInstance()
//            .collection("campsites")
//            .document()
//            .id
//        val mutableTags = mutableListOf<String>("yep", "tags", "you wish you had tags this cool")
//        testCampsite = Campsite(
//            documentID,
//            "campName",
//            "campLocation",
//            "descriptive description",
//            900000000,
//            mutableTags,
//            0.0
//        )
//        testLottery = Lottery( 2051222400000, 2051222600000, 2, mutableListOf<Campsite>(testCampsite), 2, "name", "initialBooking" )
//
//
//
//
//
//
//    }
//
//    @Test
//    fun testLotteryCreationInDbAndLotteryRepsoitorygetLottery() {
//        testLottery.createLotteryTimer()
//        lr.getLottery(testLottery.lotteryId){ lottery ->
//            if( lottery == null){
//            throw AssertionError("Lottery was null")
//        }
//
//
//            assert((lottery.endTimeLottery == testLottery.endTimeLottery) && (lottery.endTimeBooking == testLottery.endTimeBooking) && (lottery.name == testLottery.name) && (lottery.numWinners == testLottery.numWinners) && (lottery.capacity == testLottery.capacity))
//        }
//
//
//
//        //cleanup
//        FirebaseFirestore.getInstance().collection("lotteries")
//            .document(testLottery.lotteryId)
//            .delete()
//            .addOnSuccessListener {
//
//            }
//            .addOnFailureListener { e ->
//                // Handle any potential deletion failure if needed
//            }
//    }
//    @Test
//    fun testLotteryUpdateAndMulitLottery() {
//        testLottery.createLotteryTimer()
//        val campsiteList = mutableListOf<Campsite>(testLottery.campsites[0], Campsite( "docId","campnamene","campLocation2","descriptive description2",900,mutableListOf<String>("wabbafett"),1.0))
//        db.updateDrawTimer(testLottery.endTimeLottery+10000, testLottery.endTimeBooking+200000, 1, campsiteList, 3, "newName", "initialBooking")
//
//        lr.getLottery(testLottery.lotteryId) { lottery ->
//            if (lottery == null) {
//                throw AssertionError("Lottery was null")
//            }
//
//
//            assert((lottery.endTimeLottery == testLottery.endTimeLottery+10000) && (lottery.endTimeBooking == testLottery.endTimeBooking+200000) && (lottery.name == "newName") && (lottery.numWinners == 1) && (lottery.capacity == 3))
//        }
//
//            //cleanup
//        FirebaseFirestore.getInstance().collection("lotteries")
//            .document(testLottery.lotteryId)
//            .delete()
//            .addOnSuccessListener {
//
//            }
//            .addOnFailureListener { e ->
//                // Handle any potential deletion failure if needed
//            }
//    }
//    @Test
//    fun testLotteryJoin(){
//
//        testLottery.createLotteryTimer()
//        val littleLotteryGuy = LotteryEntry("madeUpUserId", Timestamp.now())
//        testLottery.addEntry(littleLotteryGuy)
//        lr.getEntries(testLottery.lotteryId){ entries ->
//            if (entries == null) {
//                throw AssertionError("entries was null")
//            }
//
//
//            assert(entries == mutableListOf<LotteryEntry>(littleLotteryGuy))
//        }
//        FirebaseFirestore.getInstance().collection("lotteries")
//            .document(testLottery.lotteryId)
//            .delete()
//            .addOnSuccessListener {
//
//            }
//            .addOnFailureListener { e ->
//                // Handle any potential deletion failure if needed
//            }
//    }
//    @Test
//    fun testGetReadables(){
//
//
//        val readableLottery = testLottery.getReadableLotteryEndTime()
//        val readableBooking = testLottery.getReadableBookingEndTime()
//        assert((readableLottery is jsqlTimestamp) && (readableBooking is jsqlTimestamp))
//        }
//}
//
////Test seeing if it will let me make an out of bounds lottery
//
////test seeing if it will let me join a lottery at capacity
//
////test seeing if it will let me join a lottery I already did
//
////test removing from a lottery