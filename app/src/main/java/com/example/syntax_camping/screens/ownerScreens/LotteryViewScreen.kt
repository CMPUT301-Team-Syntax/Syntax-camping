package com.example.syntax_camping.screens.ownerScreens

import com.example.syntax_camping.Campsite
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.dateConverterToLong

// A plain Kotlin validation function. Returns a Pair containing:
// First: A success flag (Boolean)
// Second: An error message if something went wrong, or empty string if successful
fun validateAndCreateLottery(
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

    // 1. Check for empty or non-numeric inputs
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

    // 2. Process timestamp conversion using your date converters
    val dateLongConverter = dateConverterToLong()
    val endTimeLottery = dateLongConverter.convertDateToLong(sYear, sMonth, sDay, sHour, sMin)
    val endTimeBooking = dateLongConverter.convertDateToLong(fYear, fMonth, fDay, fHour, fMin)

    // 3. Attempt creation matching your entity rules
    val newLottery = Lottery(
        endTimeLottery = endTimeLottery,
        endTimeBooking = endTimeBooking,
        numWinners = parsedWinners,
        campsites = selectedCampsites
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
