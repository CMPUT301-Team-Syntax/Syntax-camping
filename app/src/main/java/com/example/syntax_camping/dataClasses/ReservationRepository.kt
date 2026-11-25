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
        callback: (Boolean, String?) -> Unit = { _, _ -> }
    ): String {
        val reservationReference = db.collection("campsites")
            .document(documentId)
            .collection("reservations")
            .document()

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
                addReservationToAllReservations(documentId, reservation) { allSaved ->
                    if (allSaved) {
                        addReservationToEntrantData(reservation.userId, reservationId) { entrantSaved ->
                            if (entrantSaved) {
                                val csv = formatReservationAsCSV(reservation)
                                callback(true, csv)
                            } else {
                                callback(false, null)
                            }
                        }
                    } else {
                        callback(false, null)
                    }
                }
            }
            .addOnFailureListener {
                callback(false, null)
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
        cancelledStatus: String = "cancelled",
        callback: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        if (reservationId.isBlank()) {
            callback(false, null)
            return
        }

        getReservation(reservationId) { reservation ->
            if (reservation == null) {
                callback(false, null)
                return@getReservation
            }

            val resolvedStatus = cancelledStatus.ifBlank { "cancelled" }

            // Build the CSV while we still have the reservation object in memory
            val cancelledCsv = formatReservationAsCSV(reservation, overrideStatus = resolvedStatus)

            val campsiteId = reservation.campsite.documentId
            val userId = reservation.userId
            val manager = reservation.dateManager

            if (manager != null && reservation.numberGuests > 0) {
                for (dateStr in reservation.datesReserved) {
                    manager.removeReservation(dateStr, reservation.numberGuests)
                }
            }

            val persistDatesAndCleanup = {
                val batch = db.batch()

                // Keep the reservation document under the campsite, but mark its status as cancelled/rejected
                if (campsiteId.isNotBlank()) {
                    val campsiteResRef = db.collection("campsites")
                        .document(campsiteId)
                        .collection("reservations")
                        .document(reservationId)
                    batch.set(
                        campsiteResRef,
                        mapOf("status" to resolvedStatus),
                        SetOptions.merge()
                    )
                }

                // Delete from top-level reservations collection
                val topLevelResRef = db.collection("reservations").document(reservationId)
                batch.delete(topLevelResRef)

                // Remove reservationId from user's entrantData list
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
                        val resolvedLotteryId = lotteryId?.takeIf { it.isNotBlank() }
                            ?: reservation.campsite.lotteryId?.takeIf { it.isNotBlank() }

                        if (resolvedLotteryId != null && userId.isNotBlank()) {
                            lotteryRepository().updateWinnerStatus(
                                resolvedLotteryId,
                                userId,
                                resolvedStatus
                            ) { statusUpdated ->
                                callback(statusUpdated, if (statusUpdated) cancelledCsv else null)
                            }
                        } else {
                            callback(true, cancelledCsv)
                        }
                    }
                    .addOnFailureListener {
                        callback(false, null)
                    }
            }

            if (manager != null && campsiteId.isNotBlank()) {
                DateRepository().addDateManager(campsiteId, manager) { datesSaved ->
                    if (!datesSaved) {
                        callback(false, null)
                        return@addDateManager
                    }
                    persistDatesAndCleanup()
                }
            } else {
                persistDatesAndCleanup()
            }
        }
    }
    //This will return reservaion details as a string IT henddles both cancellation and confirmation, as ti will print its status
    //AS part of the csv It works with add reservation and cancel reservation to do this
    private fun formatReservationAsCSV(
        reservation: Reservation,
        overrideStatus: String? = null
    ): String {
        val csvBuilder = StringBuilder()
        csvBuilder.appendLine("Date,Number of People,Reservation Status,Campsite Name")

        val safeCampsiteName = "\"${reservation.campsite.name.replace("\"", "\"\"")}\""
        val status = overrideStatus ?: reservation.status.ifBlank { "unknown" }

        for (date in reservation.datesReserved) {
            csvBuilder.appendLine("$date,${reservation.numberGuests},$status,$safeCampsiteName")
        }

        return csvBuilder.toString().trimEnd()
    }
    private fun getReservationsForUserByStatus(
        userId: String,
        targetStatus: String,
        callback: (List<Reservation>) -> Unit
    ) {
        if (userId.isBlank()) {
            callback(emptyList())
            return
        }

        db.collection("entrantData")
            .document(userId)
            .get()
            .addOnSuccessListener { entrantDoc ->
                val reservationIds = (entrantDoc?.get("entrantReservationIds") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.filter { it.isNotBlank() }
                    ?.distinct()
                    .orEmpty()

                if (reservationIds.isEmpty()) {
                    callback(emptyList())
                    return@addOnSuccessListener
                }

                val matches = mutableListOf<Reservation>()
                var completedCount = 0

                for (id in reservationIds) {
                    getReservation(id) { reservation ->
                        synchronized(matches) {
                            if (reservation != null &&
                                reservation.status.trim().equals(targetStatus, ignoreCase = true)
                            ) {
                                matches.add(reservation)
                            }

                            completedCount++
                            if (completedCount == reservationIds.size) {
                                callback(matches.toList())
                            }
                        }
                    }
                }
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    fun getPastReservationsForUser(
        userId: String,
        callback: (List<Reservation>) -> Unit
    ) {
        getReservationsForUserByStatus(userId, "past", callback)
    }

    fun getCurrentReservationsForUser(
        userId: String,
        callback: (List<Reservation>) -> Unit
    ) {
        getReservationsForUserByStatus(userId, "current", callback)
    }

    fun getUpcomingReservationsForUser(
        userId: String,
        callback: (List<Reservation>) -> Unit
    ) {
        getReservationsForUserByStatus(userId, "upcoming", callback)
    }


}