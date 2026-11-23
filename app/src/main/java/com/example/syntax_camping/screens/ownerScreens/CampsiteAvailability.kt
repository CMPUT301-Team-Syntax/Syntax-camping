package com.example.syntax_camping.screens.ownerScreens

import com.example.syntax_camping.dataClasses.Date
import com.example.syntax_camping.dataClasses.DateManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * mode = "available": the picked dates ARE the available dates.
 * mode = "unavailable": every date from today to 6 months ahead is available except the picked ones.
 * Dates are "dd/MM/yy".
 */
fun datesForMode(mode: String, picked: List<String>): List<String> {
    if (mode == "available") return picked
    val format = DateTimeFormatter.ofPattern("dd/MM/yy")
    val pickedSet = picked.toSet()
    val result = mutableListOf<String>()
    var day = LocalDate.now()
    val last = day.plusMonths(6)
    while (!day.isAfter(last)) {
        val text = day.format(format)
        if (text !in pickedSet) result.add(text)
        day = day.plusDays(1)
    }
    return result
}

/** Builds the campsite's date list. Dates that already exist keep their bookings (currentOcc); booked dates are never dropped. */
fun buildCampsiteDates(existing: DateManager?, availableDates: List<String>, maxOcc: Int): DateManager {
    val old = existing?.allDates ?: emptyList()
    val result = mutableListOf<Date>()
    for (dateString in availableDates) {
        result.add(old.firstOrNull { it.dateString == dateString } ?: Date(dateString, maxOcc))
    }
    for (d in old) {
        if (d.currentOcc > 0 && result.none { it.dateString == d.dateString }) result.add(d)
    }
    return DateManager(result)
}