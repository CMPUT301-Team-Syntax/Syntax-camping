package com.example.syntax_camping

import com.example.syntax_camping.dataClasses.LotteryEntry
import com.example.syntax_camping.dataClasses.LotteryManager
import com.example.syntax_camping.dataClasses.Reservation
import com.example.syntax_camping.dataClasses.WaitList

/** Author: Sorachat Chaichana
 *  Entrant subclass of User superclass
 *  Should have its own specific functions involving reservations, waitlists, and lottery entries.
 *
 *  COOL THING I REALIZED ON OCT 28th:
 *  Kotlin automatically creates a return function for every variable in the parameters.
 *  For example, this file will already have entrant.deviceId, entrant.reservations, entrant.loteryEntries.
 *  It'll also inherit from User superclass and already have entrant.name, entrant.userType.
 */
class Entrant(
    val deviceId: String,
    name: String
): User(name, 0) {

    private var reservations = mutableListOf<Reservation>()
    private var lotteryEntries = mutableListOf<LotteryEntry>()  //Acts as the list to display user's lottery entries info.

    //Adds entrant's specific reservation into the Entrant's reservation list.
    fun addReservation(newReservation: Reservation){
        reservations.add(newReservation)
    }

    //Adds entrant's specific lottery Entry into chosen list. (Confusing, will be reworked.)
    fun addLotteryEntry(campsiteLottery: LotteryManager, lotteryEntry: LotteryEntry){
        campsiteLottery.addEntry(lotteryEntry)  //Add entry into campsite's lottery manager.
        lotteryEntries.add(lotteryEntry)   //Add entry into Entrant's list of entries.

    }

    //Add entrant into a specific campsite's waitlist.
    fun addWaitList(waitlist: WaitList) {
        val entrant =
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

    fun leaveLottery(campsiteLottery: LotteryManager, lotteryEntry: LotteryEntry){
        campsiteLottery.removeEntry(lotteryEntry)   //Actually removes the entry from the lottery.
        lotteryEntries.remove(lotteryEntry)  //Remove it from entrant's list of lottery entries.
    }

    fun leaveWaitlist(waitList: WaitList){
        waitList.removeUser(deviceId)
    }

    fun removeReservation(reservation: Reservation){
        reservations.remove(reservation)
    }


}