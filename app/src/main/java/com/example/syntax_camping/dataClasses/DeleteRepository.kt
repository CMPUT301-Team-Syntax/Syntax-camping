package com.example.syntax_camping.dataClasses
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.Database
import com.example.syntax_camping.ImageDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
class DeleteRepository {
    fun deleteOrAnonymizeReservation(
        reservationId: String,
        lotteryId: String? = null,
        cancelledStatus: String = "cancelled",
        reason: String = "",
        cancelledBy: String = "entrant",
        reservationRepository: ReservationRepository = ReservationRepository(),
        callback: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        if (reservationId.isBlank()) {
            callback(false, null)
            return
        }

        val db = reservationRepository.db

        reservationRepository.getReservation(reservationId) { reservation ->
            if (reservation == null) {
                callback(false, null)
                return@getReservation
            }

            val isPast = reservation.status.trim().equals("past", ignoreCase = true) ||
                    reservationRepository.hasEnded(reservation)
            val campsiteId = reservation.campsite.documentId
            val previousUserId = reservation.userId

            if (isPast) {
                val anonymizedUserId = "deleted user"
                val anonFields = mapOf(
                    "userId" to anonymizedUserId,
                    "bookingName" to "Deleted User"
                )

                val batch = db.batch()
                if (campsiteId.isNotBlank()) {
                    val campsiteResRef = db.collection("campsites")
                        .document(campsiteId)
                        .collection("reservations")
                        .document(reservationId)
                    batch.set(campsiteResRef, anonFields, SetOptions.merge())
                }

                val topLevelResRef = db.collection("reservations").document(reservationId)
                batch.set(topLevelResRef, anonFields, SetOptions.merge())

                // Unlink reservationId from departing user so deviceId reuse starts clean
                if (previousUserId.isNotBlank() && previousUserId != anonymizedUserId) {
                    val entrantRef = db.collection("entrantData").document(previousUserId)
                    batch.set(
                        entrantRef,
                        mapOf("entrantReservationIds" to FieldValue.arrayRemove(reservationId)),
                        SetOptions.merge()
                    )
                }

                batch.commit()
                    .addOnSuccessListener {
                        callback(true, reservationRepository.reservationCsv(reservation))
                    }
                    .addOnFailureListener {
                        callback(false, null)
                    }
            } else {
                reservationRepository.cancelReservation(
                    reservationId = reservationId,
                    lotteryId = lotteryId,
                    cancelledStatus = cancelledStatus,
                    reason = reason,
                    cancelledBy = cancelledBy
                ) { success, csv ->
                    if (!success || previousUserId.isBlank()) {
                        callback(success, csv)
                        return@cancelReservation
                    }

                    // Detach from entrantReservationIds so deviceId ownership is cleared on account deletion
                    db.collection("entrantData")
                        .document(previousUserId)
                        .set(
                            mapOf("entrantReservationIds" to FieldValue.arrayRemove(reservationId)),
                            SetOptions.merge()
                        )
                        .addOnCompleteListener { task ->
                            callback(task.isSuccessful, csv)
                        }
                }
            }
        }
    }
    fun deleteLottery(
        lotteryId: String,
        campsiteIds: List<String>,
        lotteryName: String = "Lottery",
        actorUserId: String = "",
        lotteryRepo: lotteryRepository = lotteryRepository(),
        onComplete: (Boolean) -> Unit = {}
    ) {
        if (lotteryId.isBlank()) {
            onComplete(false)
            return
        }

        lotteryRepo.deleteLotteryAndNotify(
            lotteryId = lotteryId,
            campsiteIds = campsiteIds,
            lotteryName = lotteryName,
            actorUserId = actorUserId,
            onComplete = onComplete
        )
    }
    fun deleteCampsite(
        campsite: Campsite,
        actorUserId: String = "",
        reason: String = "Campsite deleted",
        lotteryRepo: lotteryRepository = lotteryRepository(),
        onComplete: (Boolean) -> Unit = {}
    ) {
        val campsiteId = campsite.documentId
        if (campsiteId.isBlank()) {
            onComplete(false)
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        val campsiteRef = firestore.collection("campsites").document(campsiteId)
        val lotteryId = campsite.lotteryId?.takeIf { it.isNotBlank() }

        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        fun finishCampsiteAndOwnerCleanup(targetOwnerId: String) {
            val batch = firestore.batch()
            if (targetOwnerId.isNotBlank()) {
                batch.set(
                    firestore.collection("ownerData").document(targetOwnerId),
                    mapOf("ownerCampsiteIds" to FieldValue.arrayRemove(campsiteId)),
                    SetOptions.merge()
                )
            }
            batch.delete(campsiteRef)

            batch.commit()
                .addOnSuccessListener {
                    CampsiteRepository.getCampsites().removeAll { it.documentId == campsiteId }
                    onComplete(true)
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        }

        fun handleLotteryThenDeleteCampsite(targetOwnerId: String) {
            if (lotteryId == null) {
                finishCampsiteAndOwnerCleanup(targetOwnerId)
                return
            }

            val lotteryRef = firestore.collection("lotteries").document(lotteryId)
            lotteryRef.get()
                .addOnSuccessListener { lotteryDoc ->
                    if (!lotteryDoc.exists()) {
                        finishCampsiteAndOwnerCleanup(targetOwnerId)
                        return@addOnSuccessListener
                    }

                    val lotteryName = lotteryDoc.getString("name") ?: "Lottery"
                    val rawCampsites = (lotteryDoc.get("campsites") as? List<*>)
                        ?.mapNotNull { item ->
                            when (item) {
                                is String -> item
                                is Map<*, *> -> (item["documentId"] as? String) ?: (item["id"] as? String)
                                else -> null
                            }
                        }
                        .orEmpty()

                    val remainingCampIds = rawCampsites.filter { it.isNotBlank() && it != campsiteId }

                    if (remainingCampIds.isEmpty()) {
                        deleteLottery(
                            lotteryId = lotteryId,
                            campsiteIds = listOf(campsiteId),
                            lotteryName = lotteryName,
                            actorUserId = actorUserId.ifBlank { targetOwnerId },
                            lotteryRepo = lotteryRepo
                        ) {
                            finishCampsiteAndOwnerCleanup(targetOwnerId)
                        }
                    } else {
                        val filteredMaps = (lotteryDoc.get("campsites") as? List<*>)
                            ?.filterNot { item ->
                                val id = when (item) {
                                    is String -> item
                                    is Map<*, *> -> (item["documentId"] as? String) ?: (item["id"] as? String)
                                    else -> null
                                }
                                id == campsiteId
                            }
                            ?: emptyList<Any>()

                        lotteryRef.update("campsites", filteredMaps)
                            .addOnCompleteListener { finishCampsiteAndOwnerCleanup(targetOwnerId) }
                    }
                }
                .addOnFailureListener {
                    finishCampsiteAndOwnerCleanup(targetOwnerId)
                }
        }

        fun handleReservationsThenLottery(targetOwnerId: String) {
            val effectiveReason = if (actorUserId.isNotBlank() && targetOwnerId.isNotBlank() && actorUserId != targetOwnerId) {
                "Campsite deleted by admin"
            } else {
                reason
            }

            campsiteRef.collection("reservations").get()
                .addOnSuccessListener { subSnap ->
                    val subDocs = subSnap.documents
                    if (subDocs.isEmpty()) {
                        handleLotteryThenDeleteCampsite(targetOwnerId)
                        return@addOnSuccessListener
                    }

                    var remaining = subDocs.size
                    subDocs.forEach { resDoc ->
                        val resId = resDoc.getString("reservationId")?.takeIf { it.isNotBlank() } ?: resDoc.id
                        val status = resDoc.getString("status") ?: ""
                        val userId = resDoc.getString("userId") ?: ""
                        val datesReserved = (resDoc.get("datesReserved") as? List<*>)
                            ?.filterIsInstance<String>()
                            .orEmpty()

                        val lastDateMillis = datesReserved
                            .mapNotNull { runCatching { dateFormat.parse(it)?.time }.getOrNull() }
                            .maxOrNull()

                        val isPast = status.trim().equals("past", ignoreCase = true) ||
                                (lastDateMillis != null && lastDateMillis < startOfToday)

                        val batch = firestore.batch()
                        if (isPast) {
                            val anonFields = mapOf(
                                "userId" to "deleted user",
                                "bookingName" to "Deleted User"
                            )
                            batch.set(resDoc.reference, anonFields, SetOptions.merge())
                            batch.set(
                                firestore.collection("reservations").document(resId),
                                anonFields,
                                SetOptions.merge()
                            )
                        } else {
                            val cancelFields = mapOf(
                                "status" to "cancelled",
                                "cancelReason" to effectiveReason,
                                "cancelledBy" to if (actorUserId.isNotBlank() && targetOwnerId.isNotBlank() && actorUserId != targetOwnerId) "admin" else "owner"
                            )
                            batch.set(resDoc.reference, cancelFields, SetOptions.merge())
                            batch.set(
                                firestore.collection("reservations").document(resId),
                                cancelFields,
                                SetOptions.merge()
                            )
                            if (userId.isNotBlank() && userId != "deleted user") {
                                batch.set(
                                    firestore.collection("entrantData").document(userId),
                                    mapOf("entrantReservationIds" to FieldValue.arrayRemove(resId)),
                                    SetOptions.merge()
                                )
                            }
                        }

                        batch.commit().addOnCompleteListener {
                            remaining--
                            if (remaining == 0) {
                                handleLotteryThenDeleteCampsite(targetOwnerId)
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    handleLotteryThenDeleteCampsite(targetOwnerId)
                }
        }

        // Always fetch campsite doc so we know the real ownerId even when an admin deletes it
        campsiteRef.get()
            .addOnSuccessListener { snap ->
                val docOwnerId = snap.getString("ownerId").orEmpty()
                handleReservationsThenLottery(docOwnerId)
            }
            .addOnFailureListener {
                handleReservationsThenLottery(actorUserId)
            }
    }
    fun deleteReview(
        campsiteId: String,
        review: Review,
        actorUserId: String = "",
        isAdmin: Boolean = false,
        isOwner: Boolean = false,
        onComplete: (Boolean) -> Unit = {}
    ) {
        if (campsiteId.isBlank() || review.userId.isBlank()) {
            onComplete(false)
            return
        }

        val canDelete = isAdmin || isOwner || (actorUserId.isNotBlank() && actorUserId == review.userId)
        if (!canDelete) {
            onComplete(false)
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        val campsiteRef = firestore.collection("campsites").document(campsiteId)

        campsiteRef.get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onComplete(false)
                    return@addOnSuccessListener
                }

                val rawRatings = document.get("ratings") as? List<*> ?: emptyList<Any>()
                var removedOne = false

                val remainingRatings = rawRatings.filterNot { item ->
                    if (removedOne) return@filterNot false
                    val map = item as? Map<*, *> ?: return@filterNot false

                    val itemUserId = (map["userId"] as? String).orEmpty()
                    val itemTimestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L
                    val itemText = (map["reviewText"] as? String).orEmpty()
                    val itemRating = (map["rating"] as? Number)?.toDouble()

                    val isExactMatch = itemUserId == review.userId &&
                            itemTimestamp == review.timestamp &&
                            itemText == review.reviewText &&
                            (itemRating == null || kotlin.math.abs(itemRating - review.rating) < 1e-6)

                    if (isExactMatch) {
                        removedOne = true
                        true
                    } else {
                        false
                    }
                }

                if (!removedOne) {
                    onComplete(false)
                    return@addOnSuccessListener
                }

                val recalculatedRating = remainingRatings
                    .mapNotNull { (it as? Map<*, *>)?.get("rating") as? Number }
                    .map { it.toDouble() }
                    .let { scores -> if (scores.isEmpty()) 0.0 else scores.average() }

                campsiteRef.set(
                    mapOf(
                        "ratings" to remainingRatings,
                        "rating" to recalculatedRating
                    ),
                    SetOptions.merge()
                )
                    .addOnSuccessListener {
                        CampsiteRepository.getCampsites().find { it.documentId == campsiteId }?.let { cached ->
                            val index = cached.ratings.indexOfFirst { existing ->
                                existing.userId == review.userId &&
                                        existing.timestamp == review.timestamp &&
                                        existing.reviewText == review.reviewText &&
                                        kotlin.math.abs(existing.rating - review.rating) < 1e-6
                            }
                            if (index >= 0) {
                                cached.ratings.removeAt(index)
                            }
                            cached.refreshRating()
                        }
                        onComplete(true)
                    }
                    .addOnFailureListener {
                        onComplete(false)
                    }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }


    //Function to reset the user's info to a default state (just ignore its name)
    fun removeUser(devId: String, callback: (Result<Unit>) -> Unit) {
        val database = Database()
        val lotteryRepo = lotteryRepository()

        val defaultEntrantData = EntrantData(
            mutableListOf(),
            mutableListOf(),
            mutableListOf()
        )
        val defaultOwnerData = OwnerData(
            mutableListOf(),
            mutableListOf()
        )

        val defaultUser = User(
            devId,
            "Default",
            "Default@gmail.com",
            "1234567890",
            defaultEntrantData,
            defaultOwnerData
        )

        //Retrieve user data.
        database.fetchUserDetails(devId) { result ->
            result.onSuccess { currentUser ->

                if (currentUser == null) {
                    callback(Result.failure(Exception("User not found.")))
                    return@onSuccess
                }

                //Retrieve all IDs associated with the user.
                val entrantLotteryIds = currentUser.entrantData
                    ?.lotteryEntryIds.orEmpty().distinct()
                val reservationIds = currentUser.entrantData
                    ?.reservationIds.orEmpty().distinct()
                val ownerLotteryIds = currentUser.ownerData
                    ?.lotteryIds.orEmpty().distinct()
                val ownerCampsiteIds = currentUser.ownerData
                    ?.campsiteIds.orEmpty().distinct()

                //Keeps track of operations??
                val operations = mutableListOf<Pair<String, ((Boolean) -> Unit) -> Unit>>()

                //Internal function to reset the user to a "default" state.
                fun resetUser() {
                    database.updateUser(devId, defaultUser) { result ->
                        result.onSuccess {
                            Log.d("UserDebug", "User successfully reset.")
                            callback(Result.success(Unit))
                        }.onFailure { error ->
                            Log.e("UserDebug", "Failed to reset user", error)
                            callback(Result.failure(error))
                        }
                    }
                }

                //Called every time a cleanup operation is completed
                //Continues to the next operation regardless of success
                //DO NOT TRY TO LIMIT IT TO NO MISTAKES, SOMETHING KEEPS FAILING AND IDK WHAT IT IS
                fun processOperations(index: Int = 0) {

                    //Special case: All cleanup operations finished
                    if (index >= operations.size) {
                        resetUser()
                        return
                    }

                    val (name, operation) = operations[index]
                    Log.d("UserDebug", "Starting: $name")
                    operation { success ->

                        if (success) {
                            Log.d("UserDebug", "Completed: $name")
                        } else {
                            Log.w("UserDebug", "Cleanup failed: $name")
                        }

                        processOperations(index + 1)
                    }
                }

                //Delete entrant data!!
                //Remove user from entered lotteries.
                for (id in entrantLotteryIds) {
                    operations.add("Entrant lottery: $id" to { done ->
                        database.removeLotteryEntry(
                            id,
                            LotteryEntry(
                                devId,
                                com.google.firebase.Timestamp.now()
                            )
                        ) { success ->
                            done(success)
                        }
                    })
                }

                //Delete or anonymize the user's reservations.
                for (id in reservationIds) {
                    operations.add("Reservation: $id" to { done ->
                        deleteOrAnonymizeReservation(
                            reservationId = id,
                            reason = "User account deleted",
                            cancelledBy = "admin",
                            callback = { success, _ ->
                                done(success)
                            }
                        )
                    })
                }

                //Delete owner data!!
                //Delete the lottery individually.
                for (id in ownerLotteryIds) {
                    operations.add("Owned lottery: $id" to { done ->

                        lotteryRepo.getLottery(id) { lottery ->
                            if (lottery != null) {
                                deleteLottery(
                                    lotteryId = id,
                                    campsiteIds = lottery.campsites.map {
                                        it.documentId
                                    },
                                    lotteryName = lottery.name,
                                    onComplete = done
                                )
                            } else {
                                done(false)
                            }
                        }
                    })
                }

                //Delete owned campsites after owned lotteries.
                for (id in ownerCampsiteIds) {
                    operations.add("Owned campsite: $id" to { done ->
                        val campsite = CampsiteRepository.getCampsites()
                            .find { it.documentId == id }

                        if (campsite != null) {
                            deleteCampsite(
                                campsite = campsite,
                                reason = "User account deleted",
                                onComplete = done
                            )
                        } else {
                            Log.w("UserDebug", "Campsite not found: $id")
                            done(false)
                        }
                    })
                }

                //Delete user's profile image.
                operations.add("Profile images" to { done ->
                    ImageDatabase.deleteImages("users", devId, done)
                })

                //Delete images belonging to owned campsites.
                for (id in ownerCampsiteIds) {
                    operations.add("Campsite images: $id" to { done ->
                        ImageDatabase.deleteImages("campsites", id, done)
                    })
                }

                //TODO: Replace the user with a "DeletedUser" in every conservation they're in.
                //replaceConservationWithDeletedUser(devId)

                //TODO: Delete notifications.
                /*
                operations.add("Notifications" to { done ->
                    deleteUserNotifications(devId, done)
                })
                */

                //Start processing all cleanup and reset the user.
                processOperations()

            }.onFailure { error ->
                Log.e("UserDebug", "Failed to fetch user", error)
                callback(Result.failure(error))
            }
        }
    }


    //WORK IN PROGRESS, JUST CURRENTLY DELETES THE COLLECTION IN "users", NOWHERE ELSE.
    fun deleteUserNotifications(
        devId: String,
        onComplete: (Boolean) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(devId)
            .collection("notifications")
            .get()
            .addOnSuccessListener { snapshot ->

                val batch = db.batch()

                for (document in snapshot.documents) {
                    batch.delete(document.reference)
                }

                batch.commit()
                    .addOnSuccessListener {
                        onComplete(true)
                    }
                    .addOnFailureListener {
                        onComplete(false)
                    }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

}