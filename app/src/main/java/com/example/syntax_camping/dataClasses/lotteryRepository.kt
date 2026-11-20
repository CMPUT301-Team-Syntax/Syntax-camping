package com.example.syntax_camping.dataClasses

import com.example.syntax_camping.Campsite
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.mutableListOf
/*This repository handles the retireval of lottery information from the firebase. It works with most of the display screens to siplay the information as necessary, and also works wiht lottery, as the get lottery and getlotteries functions b
*Both return objects of lottery type, or a list of lotteries.
* */
class lotteryRepository {
    val db = FirebaseFirestore.getInstance()
    //returns all the lotteries in the database
    fun getLotteries(callback: (MutableList<Lottery>) -> Unit) {
        db.collection("lotteries")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val lotteriesList = mutableListOf<Lottery>()

                for (document in querySnapshot) {
                    val campsites = mutableListOf<Campsite>()
                    val campsitesReference = document.get("campsites") as? List<*> ?: emptyList<Any>()

                    for (campsiteItem in campsitesReference) {
                        val campsiteMap = campsiteItem as? Map<*, *> ?: continue
                        campsites.add(parseCampsiteFromMap(campsiteMap))
                    }

                    val capacity = document.getLong("capacity")?.toInt()

                    val lottery = Lottery(
                        endTimeLottery = document.getLong("endTimeLottery") ?: 0L,
                        endTimeBooking = document.getLong("endTimeBooking") ?: 0L,
                        numWinners = document.getLong("numberOfWinners")?.toInt() ?: 0,
                        campsites = campsites,
                        capacity = capacity,
                        name = document.getString("name") ?: "",
                        status = document.getString("status") ?: ""
                    ).apply {
                        this.lotteryId = document.id
                    }

                    lotteriesList.add(lottery)
                }

                callback(lotteriesList)
            }
            .addOnFailureListener {
                callback(mutableListOf())
            }
    }

    fun getLottery(lotteryId: String, callback: (Lottery?) -> Unit) {
        var lottery: Lottery? = null
        val campsites = mutableListOf<Campsite>()

        db.collection("lotteries")
            .document(lotteryId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val campsitesReference = document.get("campsites") as? List<*> ?: emptyList<Any>()

                    for (campsiteItem in campsitesReference) {
                        val campsiteMap = campsiteItem as? Map<*, *> ?: continue
                        campsites.add(parseCampsiteFromMap(campsiteMap))
                    }

                    val capacity = document.getLong("capacity")?.toInt()
                    lottery = Lottery(
                        endTimeLottery = document.getLong("endTimeLottery") ?: 0L,
                        endTimeBooking = document.getLong("endTimeBooking") ?: 0L,
                        numWinners = document.getLong("numberOfWinners")?.toInt() ?: 0,
                        campsites = campsites,
                        capacity = capacity,
                        name = document.getString("name") ?: "",
                        status = document.getString("status") ?: ""
                    ).apply {
                        this.lotteryId = document.id
                    }
                }

                callback(lottery)
            }
            .addOnFailureListener {
                callback(lottery)
            }
    }
    //Returns a list of the winners(if there are any)
    fun getWinners(lotteryId: String,callback: ( MutableList<Winner>) -> Unit) {

       // val db = FirebaseFirestore.getInstance()
        val winners = mutableListOf<Winner>()

        db.collection("lotteries")
            .document(lotteryId)
            .collection("winners")
            .get()
            .addOnSuccessListener { result ->

                for (document in result) {
                    val winner = Winner(
                        lotteryEndTime = document.getLong("responseDeadline")?.toLong() ?:0,
                        bookingEndTime = document.getLong("selectionTimestamp")?.toLong() ?: 0,
                        status = document.getString("status") ?: "",
                        userId = document.getString("userId") ?: ""
                    )
                    winners.add(winner)
                }
                    callback(winners)
            }
            .addOnFailureListener {
                callback(winners)
            }

    }
    //returns a list of the entries(if there are any)
    fun getEntries(lotteryId: String,callback: ( MutableList<LotteryEntry>) -> Unit) {

       // val db = FirebaseFirestore.getInstance()
        val entries = mutableListOf<LotteryEntry>()

        db.collection("lotteries")
            .document(lotteryId)
            .collection("entries")
            .get()
            .addOnSuccessListener { result ->

                for (document in result) {

                    val entry = LotteryEntry(
                        userId = document.getString("userId") ?: "",
                        joined = document.getTimestamp("joinedTimestamp")?: com.google.firebase.Timestamp.now()


                    )
                    entries.add(entry)
                }
                callback(entries)
            }
            .addOnFailureListener {
                callback(entries)
            }

    }
    //returns a list of the waitlsiters (if there are any)
    fun getWaitlist(lotteryId: String,callback: ( MutableList<WaitlistEntry>) -> Unit) {

      //  val db = FirebaseFirestore.getInstance()
        val entries = mutableListOf<WaitlistEntry>()

        db.collection("lotteries")
            .document(lotteryId)
            .collection("waitlist")
            .get()
            .addOnSuccessListener { result ->

                for (document in result) {

                    val entry = WaitlistEntry(
                        joinedTimestamp = document.getTimestamp("joinedTimestamp")?: com.google.firebase.Timestamp.now(),
                        status = document.getString("status") ?: "",
                        userId = document.getString("userId") ?: ""
                    )
                    entries.add(entry)
                }
                callback(entries)
            }
            .addOnFailureListener {
                callback(entries)
            }

    }
    //Basically prevents having to have repeated code for getting a campsite from firebase, especially cause the ratings, reservatiojns etc makes that code a beast
    private fun parseCampsiteFromMap(campsiteMap: Map<*, *>): Campsite {
        val campsite = Campsite(
            documentId = campsiteMap["documentId"] as? String ?: "",
            name = campsiteMap["name"] as? String ?: "",
            location = campsiteMap["location"] as? String ?: "",
            description = campsiteMap["description"] as? String ?: "",
            maxOcc = (campsiteMap["maxOccupancy"] as? Number)?.toInt() ?: 0,
            tags = ((campsiteMap["tags"] as? String) ?: "").split(",").toMutableList(),
            rating = (campsiteMap["rating"] as? Number)?.toDouble() ?: 0.0,
            images = ((campsiteMap["imagesAsStrs"] as? String) ?: "").split(",").toMutableList()
        )

        //Gets the ratings
        val rawRatings = campsiteMap["ratings"] as? List<*> ?: emptyList<Any>()
        val parsedRatings = rawRatings.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            Review(
                rating = (map["rating"] as? Number)?.toDouble() ?: 0.0,
                userId = (map["userId"] as? String) ?: "",
                reviewText = (map["reviewText"] as? String) ?: ""
            )
        }.toMutableList()
        campsite.updateRaterList(parsedRatings)

        // gets the datemaager
        val rawDateManagerMap = campsiteMap["dateManager"] as? Map<*, *>
        val rawDatesList = (rawDateManagerMap?.get("dates") as? List<*>)
            ?: (campsiteMap["dates"] as? List<*>)
            ?: emptyList<Any>()

        val parsedDates = rawDatesList.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            Date(
                maxOcc = (map["maxOcc"] as? Number)?.toInt() ?: 0,
                date = (map["date"] as? String) ?: ""
            )
        }.toMutableList()
        campsite.updateDateManager(DateManager(parsedDates))

        // 3. gets the reservations
        val rawReservations = campsiteMap["reservations"] as? List<*> ?: emptyList<Any>()
        val parsedReservations = rawReservations.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            Reservation(
                campsite = campsite,
                datesReserved = (map["datesReserved"] as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.toMutableList()
                    ?: mutableListOf(),
                numberGuests = (map["numberGuests"] as? Number)?.toInt() ?: 0,
                userId = (map["userId"] as? String) ?: "",
                status = (map["status"] as? String) ?: ""
            ).apply {
                this.reservationId = (map["reservationId"] as? String) ?: ""
            }
        }.toMutableList()
        campsite.updateReservations(parsedReservations)

        // Optional: preserve lotteryId if stored inside the nested campsite map
        val nestedLotteryId = (campsiteMap["lotteryId"] as? String)?.takeIf { it.isNotBlank() }
        campsite.updateLotteryId(nestedLotteryId)

        return campsite
    }
    //Note the two statuses that should be passed ot this are either confirmed or rejected
    fun updateWinnerStatus(
        lotteryId: String,
        userId: String,
        newStatus: String,
        callback: (Boolean) -> Unit = {}
    ) {
        if (lotteryId.isBlank() || userId.isBlank()) {
            callback(false)
            return
        }

        db.collection("lotteries")
            .document(lotteryId)
            .collection("winners")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    callback(false)
                    return@addOnSuccessListener
                }

                val batch = db.batch()
                for (document in querySnapshot.documents) {
                    batch.update(document.reference, "status", newStatus)
                }

                batch.commit()
                    .addOnSuccessListener { callback(true) }
                    .addOnFailureListener { callback(false) }
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}


