package com.example.syntax_camping

import android.content.Context
import android.provider.Settings
import com.example.syntax_camping.dataClasses.EntrantData
import com.example.syntax_camping.dataClasses.LotteryEntry
import com.example.syntax_camping.dataClasses.OwnerData
import com.example.syntax_camping.dataClasses.Reservation
import com.example.syntax_camping.dataClasses.User
import com.example.syntax_camping.dataClasses.UserProfile
import com.example.syntax_camping.dataClasses.UserType
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
    //This is to allow the use for certain firebase functions specifically fo rlottery timers
    private val functions = FirebaseFunctions.getInstance()
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
    // takes in a device id (this will be the document id), the userDetails (to store userType), and the userProfile (actually stores entrant/owner info).
    // Sets the field according to the new userDetails, and updates the user's respective entrant/owner info.

    fun updateUser(devId: String, userDetails: User, userProfile: UserProfile, callback:(Result<Unit>) -> Unit) {
        db.collection("users")
            .document(devId)
            .update(
                mapOf(
                    "displayName" to userDetails.name,
                    "userType" to when (userDetails.userType) {
                        UserType.Entrant -> 0
                        UserType.Owner -> 1
                    }
                )
            )
            .addOnSuccessListener {
                when (userDetails.userType) {
                    UserType.Entrant -> {
                        db.collection("entrantData")
                            .document(devId)
                            .update(
                                mapOf(
                                    "entrantReservationIds" to userProfile.entrantData?.reservations?.map { it.documentId },
                                    "entrantLotteryEntryIds" to userProfile.entrantData?.lotteryEntries?.map { it.userId }
                                )
                            )
                            .addOnSuccessListener { callback(Result.success(Unit)) }
                            .addOnFailureListener { error ->
                                callback(Result.failure(error))
                            }
                    }

                    UserType.Owner -> {
                        db.collection("ownerData")
                            .document(devId)
                            .update(
                                mapOf(
                                    "location" to userProfile.ownerData?.location,
                                    "campsiteIds" to (userProfile.ownerData?.campsites?.map { it.documentId })
                                )
                            )
                            .addOnSuccessListener { callback(Result.success(Unit)) }
                            .addOnFailureListener { error ->
                                callback(Result.failure(error))
                            }
                    }
                }
            }
            .addOnFailureListener { error ->
                callback(Result.failure(error) )
            }
    }

    // createNewUser
    // takes a device id, creates new document with document id equal to device id
    // sets fields equal to userDetails
    //Looks at user type afterward and add info into either entrantData or ownerData
    //Returns true or error.

    fun createNewUser(devId: String, user: User, callback: (Result<Unit>) -> Unit) {
        db.collection("users")
            .document(devId)
            .set(
                mapOf(
                    "displayName" to user.name,
                    "userType" to when (user.userType) {
                        UserType.Entrant -> 0
                        UserType.Owner -> 1
                    }
                )

            )

            //If it succeeds, add details for specific user type.
            .addOnSuccessListener {
                when (user.userType) {
                    //For entrants
                    UserType.Entrant -> {
                        val entrantData = mapOf(
                            "entrantReservationIds" to emptyList<String>(),
                            "entrantLotteryEntryIds" to emptyList<String>()
                        )

                        //Save initial document of entrantData
                        db.collection("entrantData")
                            .document(devId)
                            .set(entrantData)
                            .addOnSuccessListener {
                                callback(Result.success(Unit)) }
                            .addOnFailureListener { error ->
                                callback(Result.failure(error))
                            }

                    }
                    //For campsite owners
                    UserType.Owner -> {
                        val ownerData = mapOf(
                            "location" to "",
                            "ownerCampsiteIds" to emptyList<Campsite>())

                        db.collection("ownerData")
                            .document(devId)
                            .set(ownerData)
                            .addOnSuccessListener { callback(Result.success(Unit)) }
                            .addOnFailureListener { error ->
                                callback(Result.failure(error))
                            }
                    }
                }

                //Success!
                callback(Result.success(Unit)) }

            //Fail case
            .addOnFailureListener { error ->
                callback(Result.failure(error)) }
    }

    // fetchUserDetailsByDeviceId
    //Literally just getUser
    // calls callback function when a matching document is found.

    fun fetchUserDetailsByDeviceId(devId: String, callback: (Result<UserProfile?>) -> Unit) {
        db.collection("users")
            .document(devId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {

                    //Determine User's role
                    val userTypeValue = document.getLong("userType")?.toInt()
                        ?: run {   //User type is missing, exit function.
                            callback(Result.failure(IllegalStateException("Missing user type.")))
                            return@addOnSuccessListener
                        }


                    val userType = when (userTypeValue) {
                        0 -> UserType.Entrant
                        1 -> UserType.Owner
                        else -> { callback( Result.failure(
                                IllegalStateException("Invalid user type")
                        ))
                            return@addOnSuccessListener   //Get out of this function.
                        }
                    }

                    //Construct user object
                    val user = User(
                        userId = devId,
                        name = document.getString("displayName") ?: "New User",
                        userType = userType
                    )

                    //Retrieve role specific data.
                    when (userType) {

                        //Constructs profile with entrant data and returns it.
                        UserType.Entrant -> {
                            db.collection("entrantData")
                                .document(devId)
                                .get()
                                .addOnSuccessListener { entrantDocuments ->

                                    //What will be returned in profile.
                                    val retrievedReservations = mutableListOf<Reservation>()
                                    val retrievedLotteryEntries = mutableListOf<LotteryEntry>()

                                    //Get initial list data from Firestore
                                    val reservationIds = entrantDocuments.get("entrantReservationIds") as? List<String> ?: emptyList()
                                    val lotteryEntryIds = entrantDocuments.get("entrantLotteryEntryIds") as? List<String> ?: emptyList()

                                    //For each reservation in the list, rebuild and put into the new list as a Reservation object.
                                    reservationIds.forEach { reservationId ->   //first method of calling stuff from firebase (forEach)
                                        db.collection("reservations")
                                            .document(reservationId)
                                            .get()
                                            .addOnSuccessListener {reservationDocument ->

                                                //If it doesn't exist quit this loop.
                                                if (!reservationDocument.exists()) return@addOnSuccessListener

                                                val reservation = Reservation(
                                                    documentId = reservationId,
                                                    time = reservationDocument.getLong("time")?.toInt() ?: 0,
                                                    location = reservationDocument.getString("location") ?: "",
                                                    confirmed = reservationDocument.getBoolean("confirmed") ?: false
                                                )

                                                retrievedReservations.add(reservation)  //Adds restructered reservation into list.
                                            }
                                    }

                                    //For each entry in the list, rebuild and put into the new list as a reservation object.
                                    lotteryEntryIds.forEach { lotteryEntryId ->
                                        db.collection("lotteryEntries")
                                            .document(lotteryEntryId)
                                            .get()
                                            .addOnSuccessListener { entryDocument ->
                                                //If it doesn't exist quit this loop.
                                                if (!entryDocument.exists()) return@addOnSuccessListener

                                                val lotteryEntry = LotteryEntry(
                                                    userId = entryDocument.getString("userId") ?: "",
                                                    joined = entryDocument.getTimestamp("joined") ?: Timestamp.now()
                                                )

                                                retrievedLotteryEntries.add(lotteryEntry)
                                            }
                                    }

                                    //Reconstruct the chosen profile and return.
                                    val profile = UserProfile(
                                        user = user,
                                        entrantData = EntrantData(
                                            reservations = retrievedReservations,
                                            lotteryEntries = retrievedLotteryEntries)
                                        )
                                    callback(Result.success(profile))
                                }
                                //If this fails, send back an error
                                .addOnFailureListener { error ->
                                    callback(Result.failure(error))
                                }
                        }  //End of entrant type

                        //Constructs profiles with owner's data and returns it.
                        UserType.Owner -> {
                            db.collection("ownerData")
                                .document(devId)
                                .get()
                                .addOnSuccessListener { ownerDocument ->

                                    val retrievedLocation = ownerDocument.getString("location") ?: ""
                                    val retrievedCampsites = mutableListOf<Campsite>()


                                    //Retrieve campsites from Firestore document as a list (safe cast).
                                    val campsiteIds = (ownerDocument.get("ownerCampsiteIds") as? List<String>) ?: emptyList()

                                    //Requests all campsite in the list.
                                    val campsiteRequests = campsiteIds.map { campsiteId ->
                                        db.collection("campsites")
                                            .document(campsiteId)
                                            .get()
                                    }

                                    //When the campsite requests finishes loading, recreate each campsite.  (Method2 of calling stuff from Firebase?)
                                    Tasks.whenAllSuccess<DocumentSnapshot>(campsiteRequests)
                                        .addOnSuccessListener { campsiteDocuments ->

                                            for (campsiteDocument in campsiteDocuments) {
                                                //If this fails, exit the loop.
                                                if (!campsiteDocument.exists()) continue

                                                val campsite = Campsite(
                                                    documentId = campsiteDocument.id,
                                                    name = campsiteDocument.getString("name") ?: "",
                                                    location = campsiteDocument.getString("location") ?: "",
                                                    description = campsiteDocument.getString("description") ?: "",
                                                    maxOcc = campsiteDocument.getLong("maxOccupancy")?.toInt() ?: 0,
                                                    tags = (campsiteDocument.getString("tags") ?: "").split(",").toMutableList(),
                                                    rating = campsiteDocument.getDouble("rating") ?: 0.00,
                                                    images = (campsiteDocument.getString("images") ?: "").split(",").toMutableList()
                                                )

                                                retrievedCampsites.add(campsite)
                                            }

                                            val profile = UserProfile(
                                                user = user,
                                                ownerData = OwnerData(
                                                    location = retrievedLocation,
                                                    campsites = retrievedCampsites
                                                )
                                            )

                                            callback(Result.success(profile))

                                        }
                                }
                                //Fail
                                .addOnFailureListener { error ->
                                    callback(Result.failure(error))
                                }
                        }  //End of owner type

                    }  //end of UserType

                }

                //Document does not exist
                else {
                    callback(Result.success(null))
                }
            }
            .addOnFailureListener { error ->
                println("Error fetching user from database")
                callback(Result.failure(error))   //Database request failed to retrieve user.
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