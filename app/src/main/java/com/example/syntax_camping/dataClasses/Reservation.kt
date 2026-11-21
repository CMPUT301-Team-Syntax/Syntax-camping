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
    fun createReservation():Boolean {
        for (date in datesReserved) {
            reservationNotFull = dateManager!!.addReservation(date, numberGuests)

            if (!reservationNotFull) {
                reservationSuccessful = false
            }

        }
        if (reservationSuccessful) {
            val reservationId = reservationRepository.addReservation(
                documentId = campsite.documentId,
                reservation = this
            ) { success ->
                if (success) {

                }
            }
        }

        //Note should check status before adding reservation to user and campsite
    return reservationSuccessful}
    fun updateReservationId(newId: String) {
        this.reservationId = newId
    }
}