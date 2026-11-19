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
    //Note this oes not exist yet as I have nto done firebaseval reservationId
    var reservationSuccessful = true
    var reservationNotFull = true
    init{
        for(date in datesReserved) {
            reservationNotFull = dateManager!!.addReservation(date, numberGuests)

        if (!reservationNotFull){
            reservationSuccessful = false
        }

        }
        //Note should check status before adding reservation to user and campsite
    }
}