package com.example.syntax_camping.dataClasses

import com.google.firebase.firestore.FirebaseFirestore
import com.example.syntax_camping.Database
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.example.syntax_camping.Campsite
class ReservationRepository {
    val db = FirebaseFirestore.getInstance()

    /** Saves a lottery booking and consumes the pending win in the same transaction. */
    internal fun confirmLotteryBooking(
        reservation: Reservation,
        lotteryId: String,
        rulesAcknowledged: Boolean,
        callback: (Boolean, String?) -> Unit
    ) {
        val dates = reservation.datesReserved.toSet()
        val guests = reservation.numberGuests
        val campsite = reservation.campsite
        if (!rulesAcknowledged || dates.isEmpty() || guests <= 0 ||
            lotteryId.isBlank() || campsite.documentId.isBlank() || reservation.userId.isBlank()
        ) {
            callback(false, null)
            return
        }

        val campsiteRef = db.collection("campsites").document(campsite.documentId)
        val lotteryRef = db.collection("lotteries").document(lotteryId)
        val winnerRef = lotteryRef.collection("winners").document(reservation.userId)
        val entrantRef = db.collection("entrantData").document(reservation.userId)
        val bookingRef = db.collection("reservations").document()
        val campsiteBookingRef = campsiteRef.collection("reservations").document(bookingRef.id)

        db.runTransaction { transaction ->
            val lottery = transaction.get(lotteryRef)
            val winner = transaction.get(winnerRef)
            if (lottery.getString("status") != "booking" ||
                (lottery.getLong("endTimeBooking") ?: 0L) < System.currentTimeMillis() ||
                winner.getString("status") != "pending"
            ) {
                return@runTransaction false
            }

            val campsiteSnapshot = transaction.get(campsiteRef)
            if (!campsiteSnapshot.exists() || campsiteSnapshot.getString("lotteryId") != lotteryId) {
                return@runTransaction false
            }
            val capacity = campsiteSnapshot.getLong("maxOccupancy") ?: campsite.maxOcc.toLong()
            if (guests > capacity) return@runTransaction false

            val existingDates = campsiteSnapshot.get("dates") as? List<*> ?: emptyList<Any>()
            val lotteryDates = (lottery.get("lotteryDates") as? List<*>)?.filterIsInstance<String>().orEmpty()
            val offeredDates = lotteryDates.ifEmpty {
                existingDates.mapNotNull { (it as? Map<*, *>)?.get("date") as? String }
            }
            if (!offeredDates.containsAll(dates)) return@runTransaction false

            val remainingDates = dates.toMutableSet()
            val updatedDates = existingDates.map { item ->
                val day = item as? Map<*, *> ?: return@map item
                val date = day["date"] as? String ?: return@map item
                if (!remainingDates.remove(date)) return@map item
                val maximum = (day["maxOcc"] as? Number)?.toLong()?.takeIf { it > 0 } ?: capacity
                val occupied = (day["currentOcc"] as? Number)?.toLong() ?: 0L
                if (occupied + guests > maximum) return@runTransaction false
                mapOf("date" to date, "maxOcc" to maximum, "currentOcc" to occupied + guests)
            } + remainingDates.map { date ->
                mapOf("date" to date, "maxOcc" to capacity, "currentOcc" to guests)
            }

            val bookingData = mapOf(
                "reservationId" to bookingRef.id,
                "campsiteId" to campsiteRef.id,
                "datesReserved" to dates.toList(),
                "numberGuests" to guests,
                "userId" to reservation.userId,
                "status" to reservation.status,
                "bookingName" to reservation.bookingName,
                "rulesAcknowledged" to true
            )
            val entrantUpdate = mutableMapOf(
                "entrantReservationIds" to FieldValue.arrayUnion(bookingRef.id)
            )
            winner.getString("confirmationId")?.takeIf { it.isNotBlank() }?.let { confirmationId ->
                val confirmationRef = db.collection("pendingConfirmations").document(confirmationId)
                transaction.set(confirmationRef, mapOf("status" to "confirmed"), SetOptions.merge())
                entrantUpdate["entrantPendingEntryIds"] = FieldValue.arrayRemove(confirmationId)
            }
            transaction.update(campsiteRef, "dates", updatedDates)
            transaction.set(campsiteBookingRef, bookingData)
            transaction.set(bookingRef, bookingData)
            transaction.set(entrantRef, entrantUpdate, SetOptions.merge())
            transaction.update(winnerRef, "status", "confirmed")
            true
        }.addOnSuccessListener { confirmed ->
            if (confirmed) {
                reservation.updateReservationId(bookingRef.id)
                callback(true, formatReservationAsCSV(reservation))
            } else {
                callback(false, null)
            }
        }.addOnFailureListener { callback(false, null) }
    }

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
            "status" to reservation.status,
            "bookingName" to reservation.bookingName
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
                val cancelReason = resDoc.getString("cancelReason") ?: ""   // ADDED
                val cancelledBy = resDoc.getString("cancelledBy") ?: ""     // ADDED

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
                            this.cancelReason = cancelReason     // ADDED
                            this.cancelledBy = cancelledBy       // ADDED
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

    // CHANGED: takes a reason + who cancelled, and now KEEPS the reservation (status = cancelled)
    // instead of deleting it, so it can be listed under Settings -> cancelled reservations.
    fun cancelReservation(
        reservationId: String,
        lotteryId: String? = null,
        cancelledStatus: String = "cancelled",
        reason: String = "",                 // ADDED
        cancelledBy: String = "entrant",     // ADDED
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

            val cancelFields = mapOf(          // ADDED
                "status" to resolvedStatus,
                "cancelReason" to reason,
                "cancelledBy" to cancelledBy
            )

            val persistDatesAndCleanup = {
                val batch = db.batch()

                // Keep the reservation document under the campsite, but mark it cancelled
                if (campsiteId.isNotBlank()) {
                    val campsiteResRef = db.collection("campsites")
                        .document(campsiteId)
                        .collection("reservations")
                        .document(reservationId)
                    batch.set(campsiteResRef, cancelFields, SetOptions.merge())
                }

                // CHANGED: keep the top-level reservation too (it used to be deleted here)
                // and keep its id in the user's entrantReservationIds, same as rejectReservationByOwner.
                val topLevelResRef = db.collection("reservations").document(reservationId)
                batch.set(topLevelResRef, cancelFields, SetOptions.merge())

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

    // ADDED: the CSV text for any reservation (used by "View CSV")
    fun reservationCsv(reservation: Reservation): String = formatReservationAsCSV(reservation)

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

    // CHANGED: now takes several statuses; the single-status version below just calls it
    private fun getReservationsForUserByStatuses(
        userId: String,
        targetStatuses: List<String>,
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
                                targetStatuses.any { reservation.status.trim().equals(it, ignoreCase = true) }
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

    private fun getReservationsForUserByStatus(
        userId: String,
        targetStatus: String,
        callback: (List<Reservation>) -> Unit
    ) {
        getReservationsForUserByStatuses(userId, listOf(targetStatus), callback)
    }
    // ADDED: true when every reserved date is before today
    fun hasEnded(reservation: Reservation): Boolean {
        val format = java.text.SimpleDateFormat("dd/MM/yy", java.util.Locale.getDefault())
        val startOfToday = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        val lastDate = reservation.datesReserved
            .mapNotNull { runCatching { format.parse(it)?.time }.getOrNull() }
            .maxOrNull() ?: return false
        return lastDate < startOfToday
    }

    // ADDED: the user's completed reservations (status "past", or all dates already over)
    fun getCompletedReservationsForUser(
        userId: String,
        callback: (List<Reservation>) -> Unit
    ) {
        getReservationsForUserByStatuses(userId, listOf("past", "upcoming", "current")) { list ->
            callback(list.filter { it.status.trim().equals("past", ignoreCase = true) || hasEnded(it) })
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



    // ADDED: the user's cancelled reservations (cancelled by them or rejected by the owner)
    fun getCancelledReservationsForUser(
        userId: String,
        callback: (List<Reservation>) -> Unit
    ) {
        getReservationsForUserByStatuses(userId, listOf("cancelled", "rejected"), callback)
    }

    // CHANGED: takes a reason; records that the owner did it
    fun rejectReservationByOwner(
        reservationId: String,
        lotteryId: String? = null,
        rejectedStatus: String = "rejected",
        reason: String = "",                 // ADDED
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

            val resolvedStatus = rejectedStatus.ifBlank { "rejected" }
            val updatedCsv = formatReservationAsCSV(reservation, overrideStatus = resolvedStatus)

            val campsiteId = reservation.campsite.documentId
            val userId = reservation.userId
            val manager = reservation.dateManager

            if (manager != null && reservation.numberGuests > 0) {
                for (dateStr in reservation.datesReserved) {
                    manager.removeReservation(dateStr, reservation.numberGuests)
                }
            }

            val rejectFields = mapOf(          // ADDED
                "status" to resolvedStatus,
                "cancelReason" to reason,
                "cancelledBy" to "owner"
            )

            val persistStatusChanges = {
                val batch = db.batch()

                // Update status on the campsite's subcollection reservation doc
                if (campsiteId.isNotBlank()) {
                    val campsiteResRef = db.collection("campsites")
                        .document(campsiteId)
                        .collection("reservations")
                        .document(reservationId)
                    batch.set(campsiteResRef, rejectFields, SetOptions.merge())
                }

                // Keep the top-level reservation doc and entrantData reference so the user can see it was cancelled/rejected
                val topLevelResRef = db.collection("reservations").document(reservationId)
                batch.set(topLevelResRef, rejectFields, SetOptions.merge())

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
                                callback(statusUpdated, if (statusUpdated) updatedCsv else null)
                            }
                        } else {
                            callback(true, updatedCsv)
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
                    persistStatusChanges()
                }
            } else {
                persistStatusChanges()
            }
        }
    }

    // CHANGED: these four used to sit INSIDE rejectReservationByOwner (so nothing could call them).
    // They are now normal class functions.
    // ADDED: every reservation of a campsite, whatever its status
    fun getAllReservationsForCampsite(
        campsite: Campsite,
        callback: (List<Reservation>) -> Unit
    ) {
        if (campsite.documentId.isBlank()) {
            callback(emptyList())
            return
        }

        db.collection("campsites")
            .document(campsite.documentId)
            .collection("reservations")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val all = querySnapshot.documents.map { doc ->
                    val datesReserved = (doc.get("datesReserved") as? List<*>)
                        ?.filterIsInstance<String>()
                        ?.toMutableList()
                        ?: mutableListOf()

                    Reservation(
                        campsite = campsite,
                        datesReserved = datesReserved,
                        numberGuests = doc.getLong("numberGuests")?.toInt() ?: 0,
                        userId = doc.getString("userId") ?: "",
                        status = doc.getString("status") ?: ""
                    ).apply {
                        updateReservationId(doc.getString("reservationId") ?: doc.id)
                        this.bookingName = doc.getString("bookingName") ?: ""
                        this.cancelReason = doc.getString("cancelReason") ?: ""
                        this.cancelledBy = doc.getString("cancelledBy") ?: ""
                    }
                }
                callback(all)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    // CHANGED: now just filters the full list
    fun getReservationsForCampsiteByStatus(
        campsite: Campsite,
        targetStatus: String,
        callback: (List<Reservation>) -> Unit
    ) {
        getAllReservationsForCampsite(campsite) { all ->
            callback(all.filter { it.status.trim().equals(targetStatus, ignoreCase = true) })
        }
    }

    fun getPastReservationsForCampsite(
        campsite: Campsite,
        callback: (List<Reservation>) -> Unit
    ) {
        getReservationsForCampsiteByStatus(campsite, "past", callback)
    }

    fun getCurrentReservationsForCampsite(
        campsite: Campsite,
        callback: (List<Reservation>) -> Unit
    ) {
        getReservationsForCampsiteByStatus(campsite, "current", callback)
    }

    fun getUpcomingReservationsForCampsite(
        campsite: Campsite,
        callback: (List<Reservation>) -> Unit
    ) {
        getReservationsForCampsiteByStatus(campsite, "upcoming", callback)
    }



}