package com.example.syntax_camping.screens

import android.location.Location
import com.example.syntax_camping.Campsite
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private val typedDatePattern = Regex("""^\d{1,2}/\d{1,2}/\d{4}$""")

/** Reads a typed date like 05/08/2026. Gives null if it isn't a real date in that shape. */
fun parseSearchDate(text: String): Calendar? {
    val cleaned = text.trim()
    if (!typedDatePattern.matches(cleaned)) return null
    return try {
        val format = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        format.isLenient = false
        val date = format.parse(cleaned) ?: return null
        Calendar.getInstance().apply { time = date }
    } catch (e: Exception) {
        null
    }
}

/** Same as parseSearchDate but gives a LocalDate (used to highlight the typed range on the calendar). */
fun parseSearchLocalDate(text: String): LocalDate? {
    val cleaned = text.trim()
    if (!typedDatePattern.matches(cleaned)) return null
    val parts = cleaned.split("/")
    return runCatching { LocalDate.of(parts[2].toInt(), parts[1].toInt(), parts[0].toInt()) }.getOrNull()
}

/** Every day from start to end (inclusive) written the way campsites store them: dd/MM/yy. At most 366 days. */
fun searchDaysBetween(start: Calendar, end: Calendar): List<String> {
    val format = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    val days = mutableListOf<String>()
    val day = start.clone() as Calendar
    while (!day.after(end) && days.size < 366) {
        days.add(format.format(day.time))
        day.add(Calendar.DAY_OF_MONTH, 1)
    }
    return days
}

/** True if the campsite has every one of these days and each still has room for the guests. */
fun campsiteHasRoom(campsite: Campsite, days: List<String>, guests: Int): Boolean {
    val manager = campsite.dateManager ?: return false
    return days.all { dayString ->
        val date = manager.allDates.find { it.dateString == dayString } ?: return@all false
        // some saved dates have maxOcc 0, so fall back to the campsite's own maximum
        val capacity = if (date.maxOcc > 0) date.maxOcc else campsite.maxOcc
        capacity - date.currentOcc >= guests
    }
}

/** Straight-line distance between two map positions, in km. */
fun distanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val result = FloatArray(1)
    Location.distanceBetween(lat1, lng1, lat2, lng2, result)
    return result[0] / 1000.0
}

/** The outline of a circle on the map: list of (latitude, longitude), for drawing the search radius. */
fun circleOutline(centerLat: Double, centerLng: Double, radiusKm: Double, steps: Int = 72): List<Pair<Double, Double>> {
    val earthRadiusKm = 6371.0
    val angular = radiusKm / earthRadiusKm
    val lat1 = Math.toRadians(centerLat)
    val lng1 = Math.toRadians(centerLng)
    val points = mutableListOf<Pair<Double, Double>>()
    for (i in 0..steps) {
        val bearing = Math.toRadians(i * 360.0 / steps)
        val lat2 = asin(sin(lat1) * cos(angular) + cos(lat1) * sin(angular) * cos(bearing))
        val lng2 = lng1 + atan2(
            sin(bearing) * sin(angular) * cos(lat1),
            cos(angular) - sin(lat1) * sin(lat2)
        )
        points.add(Pair(Math.toDegrees(lat2), Math.toDegrees(lng2)))
    }
    return points
}