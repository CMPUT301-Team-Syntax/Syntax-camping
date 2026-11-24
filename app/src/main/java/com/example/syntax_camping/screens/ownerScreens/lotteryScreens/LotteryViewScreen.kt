package com.example.syntax_camping.screens.ownerScreens.lotteryScreens

import com.example.syntax_camping.Campsite
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.dateConverterToLong

const val BOOKING_LENGTH_UNIT_MS = 60_000L   // booking length is entered in MINUTES while testing; use 86_400_000L for days
fun validateAndCreateLottery(
    editingLotteryId: String? = null,
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
    capacity: String,
    selectedCampsites: MutableList<Campsite>,
    lotteryDates: List<String> = emptyList(),
    bookingLength: String = ""

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
    val lotCapacity = if (capacity.isEmpty()){
        null} else{
        capacity.toIntOrNull()
        }


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
    val opensAt = dateLongConverter.convertDateToLong(sYear, sMonth, sDay, sHour, sMin)        // entries open
    val endTimeLottery = dateLongConverter.convertDateToLong(fYear, fMonth, fDay, fHour, fMin)  // the draw
    val length = bookingLength.trim().toLongOrNull()
    if (length == null || length <= 0) {
        return Pair(false, "Booking length must be a number greater than 0")
    }
    if (opensAt >= endTimeLottery) {
        return Pair(false, "The lottery must open before it draws")
    }
    val endTimeBooking = endTimeLottery + length * BOOKING_LENGTH_UNIT_MS

    val newLottery = Lottery(
        endTimeLottery = endTimeLottery,
        endTimeBooking = endTimeBooking,
        numWinners = parsedWinners,
        campsites = selectedCampsites,
        capacity = lotCapacity,
        name = myName,
        status = null
    )

    if (editingLotteryId != null) {
        newLottery.lotteryId = editingLotteryId
    }

    newLottery.lotteryDates = lotteryDates
    newLottery.startTime = opensAt

    val success = newLottery.createLotteryTimer()

    if (!success) {
        return Pair(false, "Invalid dates: times cannot be in the past or close dates overlap")
    }

    selectedCampsites.forEach { campsite ->
        campsite.updateLotteryId(newLottery.lotteryId)
    }

    // ADDED: every campsite in the lottery gets the dates the owner picked
    if (lotteryDates.isNotEmpty()) {
        selectedCampsites.forEach { campsite -> campsite.addLotteryDates(lotteryDates) }
    }

    return Pair(true, "")
}
