package com.example.syntax_camping.screens.ownerScreens

import com.example.syntax_camping.Campsite
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.dateConverterToLong
fun validateAndCreateLottery(
    name: String,
    startDay: String,
    startMonth: String,
    startYear: String,
    startHour: String,
    startMinute: String,
    finalDay: String,
    finalMonth: String,
    finalYear: String,
    finalHour: String,
    finalMinute: String,
    numWinnersText: String,
    selectedCampsites: MutableList<Campsite>

): Pair<Boolean, String>

{
    val myName = name.trim()
    val sDay = startDay.toIntOrNull()
    val sMonth = startMonth.toIntOrNull()
    val sYear = startYear.toIntOrNull()
    val sHour = startHour.toIntOrNull()
    val sMin = startMinute.toIntOrNull()

    val fDay = finalDay.toIntOrNull()
    val fMonth = finalMonth.toIntOrNull()
    val fYear = finalYear.toIntOrNull()
    val fHour = finalHour.toIntOrNull()
    val fMin = finalMinute.toIntOrNull()

    val parsedWinners = numWinnersText.toIntOrNull()

    if (myName.isEmpty()){
        return Pair(false, "No name given to lottery")
    }
    if (myName.length < 2) {
        return Pair(false, "Lottery name must be 50 characters or fewer")
    }

    if (sDay == null || sMonth == null || sYear == null || sHour == null || sMin == null ||
        fDay == null || fMonth == null || fYear == null || fHour == null || fMin == null ||
        parsedWinners == null
    ) {
        return Pair(false, "All date and winner fields must be valid numbers")
    }

    if (parsedWinners <= 0) {
        return Pair(false, "Number of winners must be greater than 0")
    }

    if (selectedCampsites.isEmpty()){
        return Pair(false, "There are no campsites in the lottery")
    }

    val dateLongConverter = dateConverterToLong()
    val endTimeLottery = dateLongConverter.convertDateToLong(sYear, sMonth, sDay, sHour, sMin)
    val endTimeBooking = dateLongConverter.convertDateToLong(fYear, fMonth, fDay, fHour, fMin)

    val newLottery = Lottery(
        endTimeLottery = endTimeLottery,
        endTimeBooking = endTimeBooking,
        numWinners = parsedWinners,
        campsites = selectedCampsites,
        capacity = 0,
        name = myName,
        status = null
    )

    val success = newLottery.createLotteryTimer()
    if (!success) {
        return Pair(false, "Invalid dates: times cannot be in the past or close dates overlap")
    }

    selectedCampsites.forEach { campsite ->
        campsite.updateLotteryId(newLottery.lotteryId)
    }

    return Pair(true, "")
}
