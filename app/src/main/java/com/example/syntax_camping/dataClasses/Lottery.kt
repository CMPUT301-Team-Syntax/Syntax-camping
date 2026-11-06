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

/*
* This class works mostly with lottery repsoitroy and the database. It is how lottery objects are stored, and it works with campsite to store alsit of campsites. All fo its values are simultaneously stored in firebase to
* save it. It works with database to  create a new timer for an automatic draw as well
* */
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
    //wotks with database to create a lottery timer that automatically draws winners and handles the booking period in firebase
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
    //Works with the display screens to make sure that the time displayed is a more readable timestamp and not a long
    fun getReadableLotteryEndTime():Timestamp{
        readableEndTimeLottery = longDateConverter.convertLongToDate(endTimeLottery)
        return readableEndTimeLottery
    }
    fun getReadableBookingEndTime():Timestamp{
        readableEndTimeBooking = longDateConverter.convertLongToDate(endTimeBooking)
        return readableEndTimeBooking
    }
    //wokrs with databse to add an entry to this lottery(either waitlist or entries depending on lottery stage
    fun addEntry(entry: LotteryEntry, onResult: (Boolean) ->Unit){

       db.addLotteryEntry( lotteryId, entry) { success ->
           onResult(success)

       }
    }

    //works with database to remove an entry from this lottery(either waitlist or entries depdning on lottery stage
    fun removeEntry(entry: LotteryEntry, onResult: (Boolean) -> Unit) {
        db.removeLotteryEntry( lotteryId, entry){ success ->
            onResult(success)}
    }
}