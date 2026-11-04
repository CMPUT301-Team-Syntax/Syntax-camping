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
    fun addDrawTimer( endTimeLottery: Long,endTimeBooking: Long,  numberOfWinners: Int,campsites: MutableList<Campsite>): String{
        //The two following values allow firebase to generate a unique lottery id
        println("inhere")
        val lotteryReference = db.collection("lotteries").document()
        val lotteryId = lotteryReference.id
        for (campsite in campsites){
            campsite.updateLotteryId(lotteryId)
        }
        val data = mapOf(

            "endTimeLottery" to endTimeLottery,
            "endTimeBooking" to endTimeBooking,
            "numberOfWinners" to numberOfWinners,
            "status" to "initialAccepting",
            "campsites" to campsites

        )
        lotteryReference.set(data)

        return lotteryId
    }
    fun updateDrawTimer( endTimeLottery: Long,endTimeBooking: Long,  numberOfWinners: Int,campsites: MutableList<Campsite>, lotteryId: String){
        //The two following values allow firebase to generate a unique lottery id

        val lotteryReference = db.collection("lotteries").document(lotteryId)
        val lotteryId = lotteryReference.id
        for (campsite in campsites){
            campsite.updateLotteryId(lotteryId)
        }
        val data = mapOf(

            "endTimeLottery" to endTimeLottery,
            "endTimeBooking" to endTimeBooking,
            "numberOfWinners" to numberOfWinners,
            "status" to "initialAccepting",
            "campsites" to campsites

        )
        lotteryReference.update(data)


    }
    fun addLotteryEntry(lotteryId: String, entry: LotteryEntry) {
        // 1. Wait for hasUserJoinedLottery to check the database via callback
        hasUserJoinedLottery(lotteryId, entry.userId) { hasJoined ->

            // If the user has already joined, stop here and don't add them again
            if (hasJoined) {
                return@hasUserJoinedLottery
            }

            val lottery = db.collection("lotteries")
                .document(lotteryId)

            // 2. Fetch the lottery document to check its status
            lottery.get()
                .addOnSuccessListener { lotterySnapshot ->
                    val currentTime = System.currentTimeMillis()
                    val endTimeBooking = lotterySnapshot.getLong("endTimeBooking")
                    val endTimeLottery = lotterySnapshot.getLong("endTimeLottery")
                    val status = lotterySnapshot.getString("status")

                    val collectionName: String? = when (status) {
                        "initialAccepting" -> "entries"
                        "booking" -> "waitlist"
                        else -> null
                    }

                    // 3. Add the user to the correct collection
                    if (collectionName != null) {
                        lottery.collection(collectionName)
                            .document(entry.userId)
                            .set(
                                mapOf(
                                    "userId" to entry.userId,
                                    "joinedTimestamp" to entry.joined,
                                    "status" to "waiting"
                                )
                            )
                            .addOnSuccessListener {
                                // Successfully added entry!
                            }
                            .addOnFailureListener { e ->
                                // Handle failure if needed
                            }
                    }
                }
        }
    }
    fun removeLotteryEntry(lotteryId: String, entry: LotteryEntry){

        hasUserJoinedLottery(lotteryId, entry.userId) { hasJoined ->
            if (!hasJoined) {
                return@hasUserJoinedLottery
            }

            val lotteryRef = db.collection("lotteries").document(lotteryId)


            lotteryRef.collection("winners")
                .document(entry.userId)
                .get()
                .addOnSuccessListener { winnerSnapshot ->

                    // If they are a winner, stop here and do not delete
                    if (winnerSnapshot.exists()) {
                        return@addOnSuccessListener
                    }


                    lotteryRef.get()
                        .addOnSuccessListener { lotterySnapshot ->
                            val status = lotterySnapshot.getString("status")

                            val collectionName = when (status) {
                                "initialAccepting" -> "entries"
                                "booking" -> "waitlist"
                                else -> null
                            }


                            if (collectionName != null) {
                                lotteryRef.collection(collectionName)
                                    .document(entry.userId)
                                    .delete()
                                    .addOnSuccessListener {

                                    }
                                    .addOnFailureListener { e ->

                                    }
                            }
                        }
                }
        }
    }
    fun hasUserJoinedLottery(lotteryId: String, userId: String, onResult: (Boolean) -> Unit) {
        val lotteryRef = db.collection("lotteries").document(lotteryId)

        lotteryRef.collection("entries").document(userId).get().addOnSuccessListener { entry ->
            if (entry.exists()) {
                onResult(true)
                return@addOnSuccessListener
            }

            lotteryRef.collection("waitlist").document(userId).get().addOnSuccessListener { waitlist ->
                if (waitlist.exists()) {
                    onResult(true)
                    return@addOnSuccessListener
                }

                lotteryRef.collection("winners").document(userId).get().addOnSuccessListener { winner ->
                    onResult(winner.exists())
                }
            }
        }
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