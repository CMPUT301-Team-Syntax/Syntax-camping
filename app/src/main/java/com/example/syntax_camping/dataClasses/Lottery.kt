package com.example.syntax_camping.dataClasses
import android.icu.util.Calendar
import android.icu.util.TimeZone
import com.example.syntax_camping.Campsite

import com.example.syntax_camping.Database
import com.google.firebase.functions.FirebaseFunctions
import java.lang.System.console
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
//endTimeLottery and endTimeBooking can be found by feedinmg year monthd,day,hour minute into an object of the date to long converter class
class Lottery(endTimeLottery: Long, endTimeBooking:Long, numWinners: Int, campsites: MutableList<Campsite>, capacity: Int?, name: String, status: String?) {
    var lotteryId = ""
    var status = status
    val longDateConverter = dateConverterToLong()
    var capacity = capacity
    var name = name
    var endTimeLottery = endTimeLottery
    var endTimeBooking = endTimeBooking
    lateinit var readableEndTimeLottery: Timestamp
    lateinit var readableEndTimeBooking: Timestamp
    val functions = FirebaseFunctions.getInstance()
    val campsites = campsites
    val numWinners = numWinners
    //val lotteryManagers = mutableListOf<LotteryManager>()
    val campsiteIds = mutableListOf<String>()
    val db = Database()
    fun createLotteryTimer(): Boolean {

        //So this creates java calendar object that takes the input values from the user for year, day, month etc and creates an object of it to be passed to firebase as an end date for which to trigger the draw.
        //This converts the calendar object to a time in milliseconds(for the program to use)
        //Ensures that the chosen end date is not set in the past
        if (endTimeLottery <= (System.currentTimeMillis()- 3600000)) {//Note that subtraction of 36000000 occurs because of daylight savings times occurinng, and our systems having factored that in, but since other places haven't we are treating it as if it has
            return false
        }
        //Ensures that the chosen end date is not set in the past
        if ((endTimeBooking<= (System.currentTimeMillis()- 3600000)) or (endTimeBooking <= endTimeLottery)) {

            return false
        }

        lotteryId = db.addDrawTimer(endTimeLottery, endTimeBooking, numWinners,campsites,capacity, name)
        for (campsite in campsites){
            campsite.updateLotteryId(lotteryId)
            db.updateCampsiteLotteryId(campsite.documentId, lotteryId)
        }


        return true
    }
    fun getReadableLotteryEndTime():Timestamp{
        readableEndTimeLottery = longDateConverter.convertLongToDate(endTimeLottery)
        return readableEndTimeLottery
    }
    fun getReadableBookingEndTime():Timestamp{
        readableEndTimeBooking = longDateConverter.convertLongToDate(endTimeBooking)
        return readableEndTimeBooking
    }

    fun addEntry(entry: LotteryEntry):Boolean {

        val joined = db.addLotteryEntry( lotteryId, entry)
        return joined
    }

    //still implement TODO
    fun removeEntry(entry: LotteryEntry) {
        db.removeLotteryEntry( lotteryId, entry)
    }
}