package com.example.syntax_camping.dataClasses

import com.example.syntax_camping.Campsite
import java.sql.Time
import java.sql.Timestamp

/**
 * WORK IN PROGRESS
 * Lottery manager class to manage a campsite's current information on its lottery entries.
 * Works only with campsite class & LotteryEntry class.
 * Each lottery manager objects should have its own associated campsite and capacity.
 */
class LotteryManager (
    val campsite: Campsite,
    var capacity: Int,
) {
    //Private list
    private var entrantList = mutableListOf<LotteryEntry>()

    fun addEntry(entry: LotteryEntry) {
        entrantList.add(entry)
    }

    fun removeEntry(entry: LotteryEntry) {
        entrantList.remove(entry)
    }

    //Will constantly randomize the winner out of the list (if possible) and send it to Reservation.
    fun randomizeWinner() {
        //TOdo
    }

    fun updateLotteryDate(){
        //Todo
    }

    //Clear all entries.
    fun clearAllEntries(){
        entrantList.clear()
    }

}