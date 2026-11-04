package com.example.syntax_camping.dataClasses

import com.example.syntax_camping.Campsite
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlin.collections.mutableListOf

class lotteryRepository {
    private fun getLottery(lotteryId: String, callback: (Lottery?)->Unit){
        val db = FirebaseFirestore.getInstance()
        var lottery: Lottery?
        lottery = null
        val campsites= mutableListOf<Campsite>()
        db.collection("lotteries")
            .document(lotteryId)
            .get()
            .addOnSuccessListener { document ->

                val campsitesReference = document.get("campsites") as? MutableList<Campsite> ?: emptyList<Campsite>()
                for (campsiteReference in campsitesReference){
                    val campsite = Campsite(
                        documentId = document.id,
                        name = document.getString("name") ?: "",
                        location = document.getString("location") ?: "",
                        description = document.getString("description") ?: "",
                        maxOcc = document.getLong("maxOccupancy")?.toInt() ?: 0,
                        tags = (document.getString("tags") ?: "").split(",").toMutableList(),
                        document.getDouble("rating") ?: 0.00,
                        images = (document.getString("images") ?: "").split(",").toMutableList()
                    )
                    campsites.add(campsite)
                }

                    val lottery = Lottery(
                        endTimeLottery = document.getLong("endTimeLottery")?.toLong() ?:0,
                        endTimeBooking = document.getLong("endTimeBooking")?.toLong() ?: 0,
                        numWinners = document.getLong("numberOfWinners")?.toInt() ?: 0,
                        campsites
                    )

                callback(lottery)
            }
            .addOnFailureListener {
                callback(lottery)
            }

    }
    private fun getWinners(lotteryId: String,callback: ( MutableList<Winner>) -> Unit) {

        val db = FirebaseFirestore.getInstance()
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
    private fun getEntries(lotteryId: String,callback: ( MutableList<LotteryEntry>) -> Unit) {

        val db = FirebaseFirestore.getInstance()
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
    private fun getWaitlist(lotteryId: String,callback: ( MutableList<WaitlistEntry>) -> Unit) {

        val db = FirebaseFirestore.getInstance()
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