package com.example.syntax_camping.dataClasses

import com.example.syntax_camping.Campsite
import com.google.firebase.functions.FirebaseFunctions
import com.example.syntax_camping.Database

/**
 * WORK IN PROGRESS
 * Lottery manager class to manage a campsite's current information on its lottery entries.
 * Works only with campsite class & LotteryEntry class.
 * Each lottery manager objects should have its own associated campsite and capacity.
 */

//THis class is not in use rn, it will be for reservations later
class ReservationManager (
    val campsite: Campsite,
    val lotteryId: String,
    var capacity: Int,
) {
    //Private list

    private var entrantList = mutableListOf<LotteryEntry>()
    val functions = FirebaseFunctions.getInstance()
    val db = Database()


    //This function returns false if the timer fails to be created, usually due to user selecting a time in the past




    //Will constantly randomize the winner out of the list (if possible) and send it to Reservation.

    fun updateLotteryDate(){
        //Todo
    }

    //Clear all entries.
    fun clearAllEntries(){
        entrantList.clear()
    }

}