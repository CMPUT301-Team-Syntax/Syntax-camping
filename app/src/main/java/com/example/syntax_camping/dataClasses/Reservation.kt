package com.example.syntax_camping.dataClasses

import com.google.firebase.Timestamp
import com.example.syntax_camping.Campsite
/** WORK IN PROGRESS
 * Reservation data class that goes with Campsites.
 */
data class Reservation (
    val campsite: Campsite,
    val datesReserved: MutableList<String>,
    val numberGuests: Int,
    val userId: String,
    var status: String

) {
    var dateManager = campsite.dateManager
    //Note this oes not exist yet as I have nto done firebase
    var reservationId = ""
    val reservationRepository = ReservationRepository()
    var reservationSuccessful = true
    var reservationNotFull = true
    fun createReservation(callback: (Boolean, String?) -> Unit = { _, _ -> }): Boolean {
        val manager = dateManager
        if (manager == null || datesReserved.isEmpty() || numberGuests <= 0) {
            reservationSuccessful = false
            callback(false, null)
            return false
        }

        // 1. Validate all dates before mutating occupancy
        val allDatesFit = datesReserved.all { dateStr ->
            val matchingDate = manager.allDates.find { it.dateString == dateStr }
            matchingDate != null && (matchingDate.currentOcc + numberGuests) <= matchingDate.maxOcc
        }

        if (!allDatesFit) {
            reservationNotFull = false
            reservationSuccessful = false
            callback(false, null)
            return false
        }

        // 2. Apply occupancy to each requested date in memory
        for (dateStr in datesReserved) {
            if (!manager.addReservation(dateStr, numberGuests)) {
                reservationNotFull = false
                reservationSuccessful = false
                callback(false, null)
                return false
            }
        }

        reservationNotFull = true
        reservationSuccessful = true

        // 3. Persist updated dates to Firestore, then save the reservation records
        val dateRepository = DateRepository()
        dateRepository.addDateManager(campsite.documentId, manager) { datesSaved ->
            if (!datesSaved) {
                callback(false, null)
                return@addDateManager
            }

            reservationRepository.addReservation(
                documentId = campsite.documentId,
                reservation = this
            ) { resSaved, csv ->
                callback(resSaved, csv)
            }
        }

        return true
    }
    fun updateReservationId(newId: String) {
        this.reservationId = newId
    }
}