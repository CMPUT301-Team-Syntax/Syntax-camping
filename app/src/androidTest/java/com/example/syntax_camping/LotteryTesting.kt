package com.example.syntax_camping
/*
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import com.example.syntax_camping.CampsiteRepository
import android.net.Uri
import com.google.firebase.firestore.FieldPath.documentId
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.lotteryRepository

class LotteryTesting {
    private lateinit var testCampsite: Campsite
    private lateinit var db: Database
    private lateinit var testLottery: Lottery
    private lateinit var lotteryRepository: lotteryRepository

    // Set up a new wizard before every single test
    @Before
    fun setUp() {

        lotteryRepository = lotteryRepository()
        db = Database()
        val documentID = FirebaseFirestore.getInstance()
            .collection("campsites")
            .document()
            .id
        val mutableTags = mutableListOf<String>("yep", "tags", "you wish you had tags this cool")
        testCampsite = Campsite(
            documentID,
            "campName",
            "campLocation",
            "descriptive description",
            900000000,
            mutableTags,
            0.0
        )
        testLottery = Lottery( 2051222400000, 2051222600000, 2, mutableListOf<Campsite>(testCampsite), 2, "name", "initialBooking" )






    }

    @Test
    fun testLotteryCreationInDbAndLotteryRepsoitorygetLottery() {
        testLottery.createLotteryTimer()
        lotteryRepository.getLottery(testLottery.lotteryId){ lottery ->
            assert(lottery.endTimeLottery == testLottery.endTimeLottery && lottery.endTimeBooking == testLottery.endTimeBooking && lottery.name == testLottery.name && (lottery))
        }



        //cleanup
        FirebaseFirestore.getInstance().collection("campsites")
            .document(testCampsite.documentId)
            .delete()
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->
                // Handle any potential deletion failure if needed
            }
    }
    @Test
    fun testUriToString(){
        val originalPhotos = testCampsite.images


        val assertUri: Uri =
            Uri.parse("app/src/androidTest/assets/Campsitetesterphotos/IMG_6503.jpeg")
        val assertUri2: Uri =
            Uri.parse("app/src/androidTest/assets/Campsitetesterphotos/IMG_7175.jpeg")
        testCampsite.addImage(assertUri)
        testCampsite.addImage(assertUri2)
        val stringRep = testCampsite.getImagesAsStrs()
        assert((testCampsite.images != stringRep) && (stringRep.isNotEmpty()) && (stringRep[0] is String))


    }

    @Test
    fun  testUpdatingWithDatabase(){
        val originalPhotos = testCampsite.images





        testCampsite.updateName("Better Name TMTMTMTM")
        testCampsite.updateLocation("We moved. Deal with it")
        testCampsite.updateDescription("hip, fresh and new description")
        testCampsite.updateMaxOcc(1)
        testCampsite.updateTag(mutableListOf("woooooooooooo new tag"))
        db.updateCampsite(testCampsite)
        var campsites = CampsiteRepository.getCampsites()
        for (campsite in campsites) {
            if (campsite.documentId == testCampsite.documentId) {
            }
            assert((campsite.name == "Better Name TMTMTMTM") && (campsite.location == "We moved. Deal with it") && (campsite.description == "hip, fresh and new description") && (campsite.maxOcc == 1) && (campsite.tags[0] == "woooooooooooo new tag"))
        }
        //cleanup
        FirebaseFirestore.getInstance().collection("campsites")
            .document(testCampsite.documentId)
            .delete()
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->
                // Handle any potential deletion failure if needed
            }
    }

}

 */