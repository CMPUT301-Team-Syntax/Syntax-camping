package com.example.syntax_camping.dataClasses
import android.icu.util.Calendar
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.Database
import com.google.firebase.functions.FirebaseFunctions

class Lottery(campsites: MutableList<Campsite>, winners: Int) {
    var lotteryId = ""
    val functions = FirebaseFunctions.getInstance()
    val campsites = campsites
    val winners = winners
    val lotteryManagers = mutableListOf<LotteryManager>()

    val db = Database()
    fun CreateLotteryTimer(
        yearLottery: Int,
        monthLottery: Int,
        dayOfMonthLottery: Int,
        hourOfDayLottery: Int,
        minuteLottery: Int,
        yearBooking: Int,
        monthBooking: Int,
        dayOfMonthBooking: Int,
        hourOfDayBooking: Int,
        minuteBooking: Int
    ): Boolean {
        //So this creates java calendar object that takes the input values from the user for year, day, month etc and creates an object of it to be passed to firebase as an end date for which to trigger the draw.
        val lotteryCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, yearLottery)
            set(Calendar.MONTH, monthLottery)
            set(Calendar.DAY_OF_MONTH, dayOfMonthLottery)
            set(Calendar.HOUR_OF_DAY, hourOfDayLottery)
            set(Calendar.MINUTE, minuteLottery)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

        }
        val bookingCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, yearBooking)
            set(Calendar.MONTH, monthBooking)
            set(Calendar.DAY_OF_MONTH, dayOfMonthBooking)
            set(Calendar.HOUR_OF_DAY, hourOfDayBooking)
            set(Calendar.MINUTE, minuteBooking)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

        }
        //This converts the calendar object to a time in milliseconds(for the program to use)
        val endTimeLotteryMillis: Long = lotteryCalendar.timeInMillis
        //Ensures that the chosen end date is not set in the past
        if (endTimeLotteryMillis <= System.currentTimeMillis()) {
            return false
        }
        val endTimeBookingMillis: Long = bookingCalendar.timeInMillis
        //Ensures that the chosen end date is not set in the past
        if ((endTimeBookingMillis <= System.currentTimeMillis()) or (endTimeBookingMillis <= endTimeLotteryMillis)) {
            return false
        }
        lotteryId = db.addDrawTimer(endTimeLotteryMillis, endTimeBookingMillis, winners)
        return true
    }
}