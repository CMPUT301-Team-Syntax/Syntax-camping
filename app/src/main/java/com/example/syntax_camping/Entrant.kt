package com.example.syntax_camping

import com.example.syntax_camping.dataClasses.LotteryEntry
import com.example.syntax_camping.dataClasses.Reservation
import com.example.syntax_camping.dataClasses.WaitList

/** Author: Sorachat Chaichana
 *  Entrant subclass of User superclass
 *  Should have its own specific functions involving reservations, waitlists, and lottery entries.
 */
class Entrant(
    val deviceId: String,
    name: String,
    location: String,
    val reservations: MutableList<Reservation>,
    val lotteryEntries: MutableList<LotteryEntry>
): User(name, 0, location) {


    fun addReservation(newReservation: Reservation){
        reservations.add(newReservation)
    }
    fun addLotteryEntry(lotteryEntry: LotteryEntry){
        lotteryEntries.add(lotteryEntry)
    }

    //BUG1
    //fun getReservations(): MutableList<Reservation> {
    //    return reservations
    //}

    fun addWaitList(waitlist: WaitList) {
        waitlist.addUser(deviceId)
    }

    fun getPastReservations(): List<Reservation>? {
        //If any past reservations exist, return a list of it.
        if (reservations.count() > 0) {
            return reservations
        }
        //Else returns null.
        else {
            return null
        }
    }

    fun addPastReservation(reservation: Reservation){
        //Reservation list is not empty, so we'll add a past reservation before the most recent one.
        if (reservations.count() > 0) {
            reservations.add(reservations.count()-1, reservation)
        }
        //Reservation list is empty
        else {
            reservations.add(reservation)
        }
    }

    fun getPastReservation(): Reservation? {
        //If reservation list has more than one reservation, return the latest one.
        if (reservations.count() > 1) {
            return reservations[reservations.count() - 1]

        //If only one reservation was made, return that reservation
        } else if (reservations.count() == 1){
            return reservations[0]
        }
        //Else if no reservations were made, return null.
        else {
            return null
        }
    }

    fun leaveLottery(lotteryEntry: LotteryEntry){
        lotteryEntries.remove(lotteryEntry)
    }

    fun leaveWaitlist(waitList: WaitList){
        waitList.removeUser(deviceId)
    }

    fun removeReservation(reservation: Reservation){
        reservations.remove(reservation)
    }


}