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
            "status" to reservation.status,
            "bookingName" to reservation.bookingName
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
            "status" to reservation.status,
            "bookingName" to reservation.bookingName  //the name the reservation is saved under when booking
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
                val bookingName = resDoc.getString("bookingName") ?: ""

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
                                val dateStr = (map["date"] as? String) ?: ""
                                val maxOcc = (map["maxOcc"] as? Number)?.toInt() ?: 0
                                val currentOcc = (map["currentOcc"] as? Number)?.toInt() ?: 0

                                Date(date = dateStr, maxOcc = maxOcc).apply {
                                    this.currentOcc = currentOcc
                                }
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
                            this.bookingName = bookingName
                        }

                        callback(reservation)
                    }
                    .addOnFailureListener {
                        callback(null)
                    }
            }
    }
    fun getPendingConfirmation(
        pendingId: String,
        callback: (PendingConfirmation?) -> Unit
    ) {
        if (pendingId.isBlank()) {
            callback(null)
            return
        }

        db.collection("pendingConfirmations")
            .document(pendingId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val confirmation = PendingConfirmation(
                        pendingId = document.id,
                        lotteryId = document.getString("lotteryId") ?: "",
                        userId = document.getString("userId") ?: ""
                    )
                    callback(confirmation)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }
    fun deletePendingConfirmation(
        pendingId: String,
        userId: String,
        callback: (Boolean) -> Unit = {}
    ) {
        if (pendingId.isBlank()) {
            callback(false)
            return
        }

        val batch = db.batch()

        // Deletes the pending reservation for the regular collection of pendingConfirmations
        val pendingRef = db.collection("pendingConfirmations").document(pendingId)
        batch.delete(pendingRef)

        // Deletes from the entrant data
        if (userId.isNotBlank()) {
            val entrantRef = db.collection("entrantData").document(userId)
            batch.update(
                entrantRef,
                mapOf(
                    "pendingConfirmationIds" to FieldValue.arrayRemove(pendingId)
                )
            )
        }

        batch.commit()
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun cancelReservation(
        reservationId: String,
        lotteryId: String? = null,
        callback: (Boolean) -> Unit = {}
    ) {
        if (reservationId.isBlank()) {
            callback(false)
            return
        }

        getReservation(reservationId) { reservation ->
            if (reservation == null) {
                callback(false)
                return@getReservation
            }

            val campsiteId = reservation.campsite.documentId
            val userId = reservation.userId
            val manager = reservation.dateManager

            // 1. Remove guests from each reserved date in memory
            if (manager != null && reservation.numberGuests > 0) {
                for (dateStr in reservation.datesReserved) {
                    manager.removeReservation(dateStr, reservation.numberGuests)
                }
            }

            // 2. Save updated occupancy dates back to Firestore
            val persistDatesAndCleanup = {
                val batch = db.batch()

                if (campsiteId.isNotBlank()) {
                    val campsiteResRef = db.collection("campsites")
                        .document(campsiteId)
                        .collection("reservations")
                        .document(reservationId)
                    batch.delete(campsiteResRef)
                }

                val topLevelResRef = db.collection("reservations").document(reservationId)
                batch.delete(topLevelResRef)

                if (userId.isNotBlank()) {
                    val entrantRef = db.collection("entrantData").document(userId)
                    batch.set(
                        entrantRef,
                        mapOf("entrantReservationIds" to FieldValue.arrayRemove(reservationId)),
                        SetOptions.merge()
                    )
                }

                batch.commit()
                    .addOnSuccessListener {
                        // 3. If tied to a lottery, set winner status to "cancelled" so the TS script picks a replacement
                        val resolvedLotteryId = lotteryId?.takeIf { it.isNotBlank() }
                            ?: reservation.campsite.lotteryId?.takeIf { it.isNotBlank() }

                        if (resolvedLotteryId != null && userId.isNotBlank()) {
                            lotteryRepository().updateWinnerStatus(
                                resolvedLotteryId,
                                userId,
                                "cancelled"
                            ) { statusUpdated ->
                                callback(statusUpdated)
                            }
                        } else {
                            callback(true)
                        }
                    }
                    .addOnFailureListener {
                        callback(false)
                    }
            }

            if (manager != null && campsiteId.isNotBlank()) {
                DateRepository().addDateManager(campsiteId, manager) { datesSaved ->
                    if (!datesSaved) {
                        callback(false)
                        return@addDateManager
                    }
                    persistDatesAndCleanup()
                }
            } else {
                persistDatesAndCleanup()
            }
        }
    }

}