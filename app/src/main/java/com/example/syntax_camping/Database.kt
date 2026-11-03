package com.example.syntax_camping

import android.content.Context
import android.provider.Settings
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.joinToString
import com.example.syntax_camping.dataClasses.LotteryManager
import com.google.firebase.functions.FirebaseFunctions
import com.example.syntax_camping.dataClasses.LotteryEntry

// class Database
// author: Andrew
// a class used to handle all database data requests

// NOTE: we must use callbacks as firebase calls are asynchronous
// usage:
// userExistsByDeviceId(someDeviceId) { -> exists
//      if (exists)
//          code to run if user exists
//      else
//          code to run if not
// }

class Database {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    //This is to allow the use for certain firebase functions specifically fo rlottery timers
    private val functions = FirebaseFunctions.getInstance()
    // userExistsByDeviceId
    // calls callback function if there is a document with matching document id to device id

    fun userExistsByDeviceId(devId: String, callback: (Boolean) -> Unit) {
        db.collection("users")
            .document(devId)
            .get()
            .addOnSuccessListener { document ->
                callback(document.exists())
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    // getDeviceId
    // returns a string representation of user's device id

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"

    }
    //returns lottery d for use in lottery manager(track what lottery each campsite bleongs to basically
    fun addDrawTimer( endTimeLottery: Long,endTimeBooking: Long,  numberOfWinners: Int): String{
        //The two following values allow firebase to generate a unique lottery id
        val lotteryReference = db.collection("lotteries").document()
        val lotteryId = lotteryReference.id
        val data = mapOf(

            "endTimeLottery" to endTimeLottery,
            "endTimeBooking" to endTimeBooking,
            "numberOfWinners" to numberOfWinners,
            "status" to "initialAccepting"

        )
        lotteryReference.set(data)

        return lotteryId
    }
    fun addLotteryEntry(lotteryId: String, entry: LotteryEntry){
        if(hasUserJoinedLottery(lotteryId,entry.userId)){
            return
        }
        else {


            val lottery = db.collection("lotteries")
                .document(lotteryId)
            lottery.get()
                .addOnSuccessListener { lottery ->
                    val currentTime = System.currentTimeMillis()
                    val endTimeBooking = lottery.getLong("endTimeBooking")
                    val endTimeLottery = lottery.getLong("endTimeLottery")
                    val status = lottery.getString("status")
                    val collectionName: String?
                    if (status == "initialAccepting") {
                        collectionName = "entries"
                    } else if (status == "booking") {
                        collectionName = "waitlist"
                    } else {
                        return@addOnSuccessListener
                    }


                    if (collectionName != null) {
                        db.collection("lotteries")
                            .document(lotteryId)
                            .collection(collectionName)
                            .document(entry.userId)
                            .set(
                                mapOf(
                                    "userId" to entry.userId,
                                    "joinedTimestamp" to entry.joined
                                )
                            )
                    }
                }
        }
    }
    fun hasUserJoinedLottery(lotteryId: String, userId: String): Boolean{
        var isPresent = false
        val lottery = db.collection("lotteries")
            .document(lotteryId)
            lottery.collection("entries")
                .document(userId)
                .get()
                .addOnSuccessListener{entry ->
                    if (entry.exists()) {
                        isPresent = true
                        return@addOnSuccessListener
                    }

                }
        lottery.collection("waitlist")
            .document(userId)
            .get()
            .addOnSuccessListener { waitlist ->
                if (waitlist.exists()) {
                    isPresent = true
                    return@addOnSuccessListener
                }

            }
        lottery.collection("winners")
            .document(userId)
            .get()
            .addOnSuccessListener { winner ->
                if (winner.exists()) {
                    isPresent = true
                    return@addOnSuccessListener
                }

            }
        return isPresent
    }
    // updateUser
    // takes in a device id (this will be the document id)
    // and sets the field according to userDetails

    fun updateUser(devId: String, userDetails: User) {
        db.collection("users")
            .document(devId)
            .update(
                mapOf(
                    "displayName" to userDetails.name,
                    "userType" to userDetails.userType,
                )
            )
    }

    // createNewUser
    // takes a device id, creates new document with document id equal to device id
    // sets fields equal to userDetails

    fun createNewUser(devId: String, userDetails: User) {
        db.collection("users")
            .document(devId)
            .set(
                mapOf(
                    "displayName" to userDetails.name,
                    "userType" to userDetails.userType,
                )
            )
    }

    // fetchUserDetailsByDeviceId
    // calls callback function when a matching document is found.

    fun fetchUserDetailsByDeviceId(devId: String, callback: (User) -> Unit) {
        db.collection("users")
            .document(devId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("displayName") ?: "New User"
                    val userType = document.getLong("userType")?.toInt() ?: 0
                    callback(User(name, userType))
                } else {
                    callback(User("New User", 0))
                }
            }
            .addOnFailureListener { e ->
                println("Error fetching user: $e")
                callback(User("New User", 0))
            }
    }

    fun createNewCampsite(campsite: Campsite) {
        db.collection("campsites")
            .document(campsite.documentId)
            .set(
                mapOf(
                    "name" to campsite.name,
                    "location" to campsite.location,
                    "description" to campsite.description,
                    "maxOccupancy" to campsite.maxOcc,
                    "tags" to campsite.tags.joinToString(separator = ","),   // Campsite() stores tags as a MutableList, firestore stores them as a string.
                    "images" to campsite.getImagesAsStrs().joinToString(separator = ",")    // same with images
                )
            )
    }

    fun updateCampsite(campsite: Campsite) {
        db.collection("campsites")
            .document(campsite.documentId)
            .update(
                mapOf(
                    "name" to campsite.name,
                    "location" to campsite.location,
                    "description" to campsite.description,
                    "maxOccupancy" to campsite.maxOcc,
                    "tags" to campsite.tags.joinToString(separator = ","),   // Campsite() stores tags as a MutableList, firestore stores them as a string.
                    "images" to campsite.getImagesAsStrs().joinToString(separator = ",")    // same with images
                )
            )
    }


}