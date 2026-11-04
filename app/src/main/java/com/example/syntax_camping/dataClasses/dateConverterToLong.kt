package com.example.syntax_camping.dataClasses

import android.icu.util.Calendar
import android.icu.util.TimeZone
import java.sql.Timestamp


class dateConverterToLong(
                         ){
    fun convertDateToLong(yearLottery: Int,
                          monthLottery: Int,
                          dayOfMonthLottery: Int,
                          hourOfDayLottery: Int,
                          minuteLottery: Int):Long {
        val lotteryCalendar = Calendar.getInstance(
            TimeZone.getTimeZone("America/Edmonton")
        ).apply {
            set(Calendar.YEAR, yearLottery)
            set(Calendar.MONTH, (monthLottery - 1))//-1 Because starts month count at 0 for kotlin
            set(Calendar.DAY_OF_MONTH, dayOfMonthLottery)
            set(Calendar.HOUR_OF_DAY, hourOfDayLottery - 1)//-1 to adjust for daylight savings time no longer happening, but the edmonton america tiemzone still having it
            set(Calendar.MINUTE, minuteLottery)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

        }
        val longTime: Long = lotteryCalendar.timeInMillis
        //println(longTime)
        return longTime
    }
    fun convertLongToDate(longToConvert: Long): Timestamp{




// Convert Long to Timestamp
        val timestamp = Timestamp(longToConvert)
        return timestamp

    }
}