package com.example.syntax_camping.dataClasses

import com.google.firebase.firestore.FirebaseFirestore
import com.example.syntax_camping.Database
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.example.syntax_camping.Campsite
class ReservationRepository {
    val db = FirebaseFirestore.getInstance()

    fun addReservation(
        documentId: String,
        reservation: Reservation,
        callback: (Boolean) -> Unit = {}
    ): String {
        val reservationReference = db.collection("campsites")
            .document(documentId)
            .collection("reservations")
            .document()

        // Generate the single source-of-truth ID once
        val reservationId = reservationReference.id
        reservation.updateReservationId(reservationId)

        val data = mapOf(
            "reservationId" to reservationId,
            "campsiteId" to documentId,
            "datesReserved" to reservation.datesReserved,
            "numberGuests" to reservation.numberGuests,
            "userId" to reservation.userId,
            "status" to reservation.status
        )

        reservationReference.set(data)
            .addOnSuccessListener {
                // Step 2: Save to the top-level "reservations" collection using the same ID
                addReservationToAllReservations(documentId, reservation) { allSaved ->
                    if (allSaved) {
                        // Step 3: Append the same ID to the user's "entrantData"
                        addReservationToEntrantData(reservation.userId, reservationId, callback)
                    } else {
                        callback(false)
                    }
                }
            }
            .addOnFailureListener {
                callback(false)
            }

        return reservationId
    }

    //  So you cna search for a reservation by Id
    fun addReservationToAllReservations(
        campsiteId: String,
        reservation: Reservation,
        callback: (Boolean) -> Unit = {}
    ): String {
        val docRef = if (reservation.reservationId.isNotBlank()) {
            db.collection("reservations").document(reservation.reservationId)
        } else {
            db.collection("reservations").document()
        }

        val reservationId = docRef.id
        reservation.updateReservationId(reservationId)

        val data = mapOf(
            "reservationId" to reservationId,
            "campsiteId" to campsiteId,
            "datesReserved" to reservation.datesReserved,
            "numberGuests" to reservation.numberGuests,
            "userId" to reservation.userId,
            "status" to reservation.status
        )

        docRef.set(data)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }

        return reservationId
    }

    // appends the reservationID to entrant data(for the user to be able to access their reservations
    fun addReservationToEntrantData(
        userId: String,
        reservationId: String,
        callback: (Boolean) -> Unit = {}
    ) {
        if (userId.isBlank()) {
            callback(false)
            return
        }

        val entrantUpdate = mapOf(
            "entrantReservationIds" to FieldValue.arrayUnion(reservationId)
        )

        db.collection("entrantData")
            .document(userId)
            .set(entrantUpdate, SetOptions.merge())
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
        //This is complicated because reseervation has a campsite tied to it, which must be retrieved
    fun getReservation(reservationId: String, callback: (Reservation?) -> Unit) {
        if (reservationId.isBlank()) {
            callback(null)
            return
        }

        db.collection("reservations")
            .document(reservationId)
            .get()
            .addOnSuccessListener { resDoc ->
                if (resDoc == null || !resDoc.exists()) {
                    callback(null)
                    return@addOnSuccessListener
                }

                val campsiteId = resDoc.getString("campsiteId") ?: ""
                val datesReserved = (resDoc.get("datesReserved") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.toMutableList()
                    ?: mutableListOf()
                val numberGuests = resDoc.getLong("numberGuests")?.toInt() ?: 0
                val userId = resDoc.getString("userId") ?: ""
                val status = resDoc.getString("status") ?: ""

                // Fetch the associated Campsite so we can construct the full Reservation object
                db.collection("campsites")
                    .document(campsiteId)
                    .get()
                    .addOnSuccessListener { campDoc ->
                        val campsite = if (campDoc != null && campDoc.exists()) {
                            val tagsList = campDoc.getString("tags")
                                ?.takeIf { it.isNotBlank() }
                                ?.split(",")
                                ?.map { it.trim() }
                                ?.toMutableList()
                                ?: mutableListOf()

                            val rawDateManagerMap = campDoc.get("dateManager") as? Map<*, *>
                            val rawDatesList = (rawDateManagerMap?.get("dates") as? List<*>)
                                ?: (campDoc.get("dates") as? List<*>)
                                ?: emptyList<Any>()

                            val parsedDates = rawDatesList.mapNotNull { item ->
                                val map = item as? Map<*, *> ?: return@mapNotNull null
                                Date(
                                    maxOcc = (map["maxOcc"] as? Number)?.toInt() ?: 0,
                                    date = (map["date"] as? String) ?: ""
                                )
                            }
                            val dateManager = DateManager(parsedDates.toMutableList())

                            Campsite(
                                documentId = campDoc.id,
                                name = campDoc.getString("name") ?: "",
                                location = campDoc.getString("location") ?: "",
                                description = campDoc.getString("description") ?: "",
                                maxOcc = campDoc.getLong("maxOccupancy")?.toInt() ?: 0,
                                tags = tagsList,
                                rating = campDoc.getDouble("rating") ?: 0.0,
                                images = (campDoc.get("images") as? List<*>)
                                    ?.filterIsInstance<String>()
                                    ?.toMutableList()
                                    ?: mutableListOf()
                            ).apply {
                                updateLotteryId(
                                    campDoc.getString("lotteryId")?.takeIf { it.isNotBlank() })
                                updateDateManager(dateManager)
                            }
                        } else {
                            // Fallback minimal Campsite if the campsite document was deleted
                            Campsite(
                                documentId = campsiteId,
                                name = "",
                                location = "",
                                description = "",
                                maxOcc = 0,
                                tags = mutableListOf(),
                                rating = 0.0,
                                images = mutableListOf()
                            ).apply {
                                updateDateManager(DateManager(mutableListOf()))
                            }
                        }

                        val reservation = Reservation(
                            campsite = campsite,
                            datesReserved = datesReserved,
                            numberGuests = numberGuests,
                            userId = userId,
                            status = status
                        ).apply {
                            updateReservationId(resDoc.id)
                        }

                        callback(reservation)
                    }
                    .addOnFailureListener {
                        callback(null)
                    }
            }
    }

}