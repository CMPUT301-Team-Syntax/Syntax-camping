package com.example.syntax_camping.dataClasses

import com.example.syntax_camping.Campsite
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.mutableListOf

class lotteryRepository {
    val db = FirebaseFirestore.getInstance()
    fun getLotteries(callback: ( MutableList<Lottery>) -> Unit){
        db.collection("lotteries")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val lotteriesList = mutableListOf<Lottery>()

                // Loop through every document in the "lotteries" collection
                for (document in querySnapshot) {
                    val campsites = mutableListOf<Campsite>()

                    // 1. Extract the array of maps from this specific document
                    val campsitesReference = document.get("campsites") as? List<*> ?: emptyList<Any>()

                    for (campsiteItem in campsitesReference) {
                        val campsiteMap = campsiteItem as? Map<*, *> ?: continue
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
                        campsites.add(campsite)
                    }

                    val capacityLong = document.getLong("capacity")
                    val capacity = capacityLong?.toInt()

                    // 2. Build the Lottery object for this document
                    val lottery = Lottery(
                        endTimeLottery = document.getLong("endTimeLottery") ?: 0L,
                        endTimeBooking = document.getLong("endTimeBooking") ?: 0L,
                        numWinners = document.getLong("numberOfWinners")?.toInt() ?: 0,
                        campsites = campsites,
                        capacity = capacity,
                        name = document.getString("name") ?: "",
                        status = document.getString("status")?: ""
                    ).apply {
                        this.lotteryId = document.id
                    }

                    // 3. Add it to our master list
                    lotteriesList.add(lottery)
                }

                // 4. Return the full list via callback once all documents are processed
                callback(lotteriesList)
            }
            .addOnFailureListener {
                // Return an empty list if the query fails
                callback(mutableListOf())
            }
    }
    fun getLottery(lotteryId: String, callback: (Lottery?)->Unit){

        var lottery: Lottery?
        lottery = null
        val campsites= mutableListOf<Campsite>()
        db.collection("lotteries")
            .document(lotteryId)
            .get()
            .addOnSuccessListener { document ->

                val campsitesReference = document.get("campsites") as? List<*> ?: emptyList<Any>()
                for (campsiteItem in campsitesReference){
                    val campsiteMap = campsiteItem as? Map<*, *> ?: continue
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
                    campsites.add(campsite)
                }
                    val capacityLong = document.getLong("capacity")
                    val capacity = capacityLong?.toInt()
                    lottery = Lottery(
                        endTimeLottery = document.getLong("endTimeLottery")?.toLong() ?:0,
                        endTimeBooking = document.getLong("endTimeBooking")?.toLong() ?: 0,
                        numWinners = document.getLong("numberOfWinners")?.toInt() ?: 0,
                        campsites,
                        capacity = capacity,
                        name= document.getString("name")?: "",
                        status = document.getString("status")?: ""
                    ).apply {
                        this.lotteryId = document.id
                    }

                callback(lottery)
            }
            .addOnFailureListener {
                callback(lottery)
            }

    }
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
}

/*
lr = lotteryRepository()
val lottery = lr.getLottery(campsite.lotteryId)
* if (lottery != null){
    lottery.name
    lottery.status (can be "initialBooking"(first joining, no names yet), status "booking" (names/winners drawn. wailist), "complete"
    lottery.getReadableEndLotteryTime
    lottery..getReadableBookingTime

}

*
* */

