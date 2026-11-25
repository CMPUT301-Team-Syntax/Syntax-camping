package com.example.syntax_camping.screens.ownerScreens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import java.util.Locale

/** Turns a typed address into (latitude, longitude). Returns null if it can't be found. Call off the main thread. */
fun geocodeAddress(context: Context, address: String): Pair<Double, Double>? {
    if (address.isBlank() || !Geocoder.isPresent()) return null
    return try {
        @Suppress("DEPRECATION")
        val results = Geocoder(context, Locale.getDefault()).getFromLocationName(address, 1)
        val first = results?.firstOrNull() ?: return null
        Pair(first.latitude, first.longitude)
    } catch (e: Exception) {
        null
    }
}

/**
 * Address suggestions for what the owner has typed so far (up to 5).
 * Each one is Triple(full address text, latitude, longitude). Call off the main thread.
 */
fun suggestAddresses(context: Context, query: String): List<Triple<String, Double, Double>> {
    if (query.isBlank() || !Geocoder.isPresent()) return emptyList()
    return try {
        @Suppress("DEPRECATION")
        val results = Geocoder(context, Locale.getDefault()).getFromLocationName(query, 5)
        (results ?: emptyList()).mapNotNull { a ->
            val line = a.getAddressLine(0)
            if (line.isNullOrBlank()) null else Triple(line, a.latitude, a.longitude)
        }.distinctBy { it.first }
    } catch (e: Exception) {
        emptyList()
    }
}

/** Opens Google Maps on the campsite. Uses coordinates if we have them, otherwise searches the address text. */
fun openInGoogleMaps(context: Context, latitude: Double?, longitude: Double?, label: String, address: String) {
    val hasCoords = latitude != null && longitude != null
    val geoUri = if (hasCoords) {
        Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})")
    } else {
        Uri.parse("geo:0,0?q=${Uri.encode(address)}")
    }
    val mapsIntent = Intent(Intent.ACTION_VIEW, geoUri).setPackage("com.google.android.apps.maps")
    try {
        context.startActivity(mapsIntent)
    } catch (e: ActivityNotFoundException) {
        // no Google Maps app: open it in the browser instead
        val query = if (hasCoords) "$latitude,$longitude" else address
        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        } catch (e2: ActivityNotFoundException) {
            // nothing can open a map on this device
        }
    }
}