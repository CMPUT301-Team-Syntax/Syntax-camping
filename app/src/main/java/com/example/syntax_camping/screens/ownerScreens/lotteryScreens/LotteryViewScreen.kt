package com.example.syntax_camping.screens.ownerScreens.lotteryScreens

import com.example.syntax_camping.Campsite
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.dateConverterToLong

// The first date/time is the DRAW (the countdown ends there). The second is the LAST DAY TO BOOK
// for winners and waitlisters. The lottery is open for entries from the moment it is created.
fun validateAndCreateLottery(
    editingLotteryId: String? = null,
    name: String,
    drawDay: String,
    drawMonth: String,
    drawYear: String,
    drawHour: String,
    drawMinute: String,
    bookByDay: String,
    bookByMonth: String,
    bookByYear: String,
    bookByHour: String,
    bookByMinute: String,
    numWinnersText: String,
    capacity: String,
    selectedCampsites: MutableList<Campsite>,
    lotteryDates: List<String> = emptyList()

): Pair<Boolean, String>

{
    val myName = name.trim()
    val dDay = drawDay.toIntOrNull()
    val dMonth = drawMonth.toIntOrNull()
    val dYear = drawYear.toIntOrNull()
    val dHour = drawHour.toIntOrNull()
    val dMin = drawMinute.toIntOrNull()

    val bDay = bookByDay.toIntOrNull()
    val bMonth = bookByMonth.toIntOrNull()
    val bYear = bookByYear.toIntOrNull()
    val bHour = bookByHour.toIntOrNull()
    val bMin = bookByMinute.toIntOrNull()

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

    if (dDay == null || dMonth == null || dYear == null || dHour == null || dMin == null ||
        bDay == null || bMonth == null || bYear == null || bHour == null || bMin == null ||
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
    val endTimeLottery = dateLongConverter.convertDateToLong(dYear, dMonth, dDay, dHour, dMin)   // the draw
    val endTimeBooking = dateLongConverter.convertDateToLong(bYear, bMonth, bDay, bHour, bMin)   // last time to book

    if (endTimeBooking <= endTimeLottery) {
        return Pair(false, "The last day to book must be after the draw")
    }

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
    newLottery.startTime = 0L   // no separate opening time: entries are open as soon as the lottery exists

    val success = newLottery.createLotteryTimer()

    if (!success) {
        return Pair(false, "Invalid dates: times cannot be in the past or close dates overlap")
    }

    selectedCampsites.forEach { campsite ->
        campsite.updateLotteryId(newLottery.lotteryId)
    }

    // every campsite in the lottery gets the dates the owner picked
    if (lotteryDates.isNotEmpty()) {
        selectedCampsites.forEach { campsite -> campsite.addLotteryDates(lotteryDates) }
    }

    return Pair(true, "")
}