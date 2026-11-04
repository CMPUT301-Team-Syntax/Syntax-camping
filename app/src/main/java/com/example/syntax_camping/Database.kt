package com.example.syntax_camping

import android.content.Context
import android.provider.Settings
import com.example.syntax_camping.dataClasses.EntrantData
import com.example.syntax_camping.dataClasses.LotteryEntry
import com.example.syntax_camping.dataClasses.OwnerData
import com.example.syntax_camping.dataClasses.Reservation
import com.example.syntax_camping.dataClasses.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.joinToString
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.example.syntax_camping.dataClasses.LotteryManager
import com.google.firebase.functions.FirebaseFunctions

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
    //This is to allow the use for certain firebase functions specifically fo lottery timers
    private val functions = FirebaseFunctions.getInstance()

    //This is for userCache related things.
    private var _users: MutableList<User> = mutableListOf()
    val users: List<User>
        get() = _users

    // userExistsByDeviceId
    // calls callback function if there is a document with matching document id to device id

    fun userExistsByDeviceId(devId: String, callback: (Boolean) -> Unit) {
        db.collection("users")
            .document(devId)
            .get()
            .addOnSuccessListener { document ->
                callback(document.exists())   //User exists & firebase is working
            }
            .addOnFailureListener { error ->
                println("Error checking user: ${error.message}")   //Returns an error message (aka user does not exist).
                callback(false)   //Returns either user failure / database failure
            }
    }

    // getDeviceId
    // returns a string representation of user's device id

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: throw IllegalStateException("Unable to retrieve device ID")  //Changed this to return an error instead of potentially having users with "unknown" as their Id.

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
    // takes in a device id (this will be the document id), the userDetails (to store userType), and the userProfile (actually stores entrant/owner info).
    // Gives back a result of either true or false.
    // Update the new user details.
    // Depending on if entrant data or owner data exists or not, update those too.
    fun updateUser(devId: String, userDetails: User, callback:(Result<Unit>) -> Unit) {
        db.collection("users")
            .document(devId)
            .update(
                mapOf(
                    "displayName" to userDetails.name,
                    "displayEmail" to userDetails.email,
                    "displayPhoneNumber" to userDetails.phoneNumber,
                )
            )
            .addOnSuccessListener {

                //Inside function that updates owner data.
                //This can happen independently of its own, or it can happen after entrant successfully updates.
                fun updateOwnerData() {
                    //Update owner data if it exists.
                    val ownerData = userDetails.ownerData
                    if (ownerData != null) {
                        db.collection("ownerData")
                            .document(devId)
                            .update(
                                mapOf(
                                    "lotteryIds" to userDetails.ownerData.lotteryIds,
                                    "ownerCampsiteIds" to userDetails.ownerData.campsiteIds
                                )
                            )
                            .addOnSuccessListener {
                                callback(Result.success(Unit))
                            }
                            .addOnFailureListener { error ->
                                println("Error updating owner data: ${error.message}")
                                callback(Result.failure(error))
                            }
                    }
                    else {
                        callback(Result.success(Unit))
                    }
                }

                //Update entrant data if it exists.
                val entrantData = userDetails.entrantData
                if (entrantData != null) {
                    db.collection("entrantData")
                        .document(devId)
                        .update(
                            mapOf(
                                "entrantReservationIds" to userDetails.entrantData.reservationIds,
                                "entrantLotteryEntryIds" to userDetails.entrantData.lotteryEntryIds
                            )
                        )
                        .addOnSuccessListener {
                            updateOwnerData()   //Update owner data after entrant succeeds updating.
                        }
                        .addOnFailureListener { error ->
                            println("Error updating entrant data: ${error.message}")
                            callback(Result.failure(error))
                        }
                }
                else {
                    updateOwnerData()   //In case the entrant data doesn't exist, try updating owner data.
                }
            }
            .addOnFailureListener { error ->
                println("Error updating user: ${error.message}")
                callback(Result.failure(error))
            }
    }

    // createNewUser
    //Takes a device id, then first creates the user document for it.
    //If that succeeds, then it creates the entrantData document for that user.
    //If all previous steps succeed, it creates the ownerData document for that user.
    fun createNewUser(devId: String, user: User, callback: (Result<Unit>) -> Unit) {
        db.collection("users")
            .document(devId)
            .set(
                mapOf(
                    "displayName" to user.name,
                    "displayEmail" to user.email,
                    "displayPhoneNumber" to user.phoneNumber,
                )
            )
            //If it succeeds, initialize details for specific user type in their own collections.
            .addOnSuccessListener {
                //Save initial document of entrantData
                db.collection("entrantData")
                    .document(devId)
                    .set(
                        mapOf(
                            "entrantReservationIds" to emptyList<String>(),
                            "entrantLotteryEntryIds" to emptyList<String>()
                        )
                    )
                    .addOnSuccessListener {
                        //Initialize campsite owner's data.
                        db.collection("ownerData")
                            .document(devId)
                            .set(
                                mapOf(
                                    "lotteryIds" to emptyList<String>(),
                                    "ownerCampsiteIds" to emptyList<String>()
                                )
                            )
                            .addOnSuccessListener {
                                //All documents successfully created!
                                callback(Result.success(Unit))
                            }
                            .addOnFailureListener { error ->
                                println("Error creating document for campsite owner: ${error.message}")
                                callback(Result.failure(error))
                            }
                    }
                    .addOnFailureListener { error ->
                        println("Error creating document for entrant: ${error.message}")
                        callback(Result.failure(error))
                    }
            }
            .addOnFailureListener { error ->
                println("Error creating document for user: ${error.message}")
                callback(Result.failure(error))
            }
    }

    // fetchUserDetails
    // Gets user details including the user's entrant or owner data.
    // Returns a callback of user on success if it works.
    // Returns a callback of a failed result if it fails.

    fun fetchUserDetails(devId: String, callback: (Result<User?>) -> Unit) {
        db.collection("users")
            .document(devId)
            .get()
            .addOnSuccessListener { userDocument ->
                if (!userDocument.exists()) {
                    callback(Result.success(null))
                    return@addOnSuccessListener
                }

                //Retrieve shared user info.
                val name = userDocument.get("displayName") as? String ?: ""
                val email = userDocument.get("displayEmail") as? String ?: ""
                val phoneNumber = userDocument.get("displayPhoneNumber") as? String ?: ""
                var fetchedEntrantData: EntrantData? = null
                var fetchedOwnerData: OwnerData? = null

                //Reconstruct the entrant and campsite owner's info.
                db.collection("entrantData")
                    .document(devId)
                    .get()
                    .addOnSuccessListener { entrantDocument ->

                        if (entrantDocument.exists()) {
                            val reservationIds =
                                entrantDocument.get("entrantReservationIds") as? List<String>
                                    ?: emptyList()
                            val lotteryEntryIds =
                                entrantDocument.get("entrantLotteryEntryIds") as? List<String>
                                    ?: emptyList()
                            fetchedEntrantData = EntrantData(
                                reservationIds.toMutableList(),
                                lotteryEntryIds.toMutableList()
                            )
                        }

                        //Reconstructs campsite owner's info.
                        db.collection("ownerData")
                            .document(devId)
                            .get()
                            .addOnSuccessListener { ownerDocument ->

                                if (ownerDocument.exists()) {
                                    val lotteryIds =
                                        ownerDocument.get("lotteryIds") as? List<String>
                                            ?: emptyList()
                                    val campsiteIds =
                                        ownerDocument.get("ownerCampsiteIds") as? List<String>
                                            ?: emptyList()
                                    fetchedOwnerData = OwnerData(
                                        lotteryIds.toMutableList(),
                                        campsiteIds.toMutableList()
                                    )
                                }

                                //Recreate user.
                                val fetchedUser = User(
                                    devId,
                                    name,
                                    email,
                                    phoneNumber,
                                    fetchedEntrantData,
                                    fetchedOwnerData
                                )
                                callback(Result.success(fetchedUser))
                            }
                            .addOnFailureListener { error ->
                                println("Error fetching campsite owner data: ${error.message}")
                                callback(Result.failure(error))
                            }
                    }
                    .addOnFailureListener { error ->
                        println("Error fetching entrant data: ${error.message}")
                        callback(Result.failure(error))
                    }
            }
            .addOnFailureListener { error ->
                println("Error fetching user details: ${error.message}")
                callback(Result.failure(error))   //Database request failed to retrieve user.
            }
    }

    // Refreshes the user cache by retrieving the latest user data from Firestore.
    // Reuses fetchUserDetails to constantly fetch every user in the whole collection.
    // Returns a callback of true if _user list is successfully refreshed.
    // Returns a callback of false if it fails.
    fun refreshUserCache(callback: (Boolean) -> Unit = {}) {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val loadedUsers = mutableListOf<User>()
                var completedRequests = 0
                var successful = true

                //empty collection
                if (result.isEmpty) {
                    _users.clear()
                    callback(true)
                    return@addOnSuccessListener
                }

                //For every document, retrieve its user.
                for (document in result) {
                    val devId = document.id
                    fetchUserDetails(devId) {fetchResult ->
                        fetchResult.onSuccess {user ->
                            if (user != null) {
                                loadedUsers.add(user)
                            }
                        }.onFailure {
                            successful = false
                        }
                        completedRequests++   //Adds a count into completed requests.

                        //If everything completed successfully, add all loaded Users into _users.
                        if (completedRequests == result.size()) {
                            if (successful) {
                                _users.clear()
                                _users.addAll(loadedUsers)
                            }

                            callback(successful)
                        }
                    }
                }
            }
            .addOnFailureListener { error ->
                println("Error refreshing user cache: ${error.message}")
                callback(false)
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