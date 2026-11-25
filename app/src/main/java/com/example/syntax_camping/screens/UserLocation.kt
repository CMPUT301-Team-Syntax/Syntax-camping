package com.example.syntax_camping.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.util.Consumer

/** True if the user has allowed approximate or precise location. */
fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

/**
 * Finds where the user is right now. Gives back Pair(latitude, longitude), or null if we couldn't
 * (no permission, location switched off, or no fix).
 */
@SuppressLint("MissingPermission")
fun fetchCurrentLocation(context: Context, onResult: (Pair<Double, Double>?) -> Unit) {
    if (!hasLocationPermission(context)) {
        onResult(null)
        return
    }
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    if (manager == null) {
        onResult(null)
        return
    }
    try {
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .filter { manager.isProviderEnabled(it) }
        if (providers.isEmpty()) {
            onResult(null)
            return
        }
        // a typed null: a bare null matches two overloads (android.os and androidx.core.os CancellationSignal)
        val noCancel: CancellationSignal? = null

        // CHANGED: Consumer<Location?> instead of Consumer<Location>. Android passes null when it has no fresh fix,
// so the interface has to allow null. That is what "overrides nothing" was complaining about.
        val receiver = object : Consumer<Location?> {
            // CHANGED: parameter renamed from "fresh" to "value" to match the interface, and typed Location?
            override fun accept(value: Location?) {
                // if there is no fresh fix, fall back to the last position the phone knew
                val found = value ?: try {   // CHANGED: was "fresh ?: try"
                    providers.mapNotNull { manager.getLastKnownLocation(it) }.firstOrNull()
                } catch (e: SecurityException) {
                    null
                }
                onResult(found?.let { Pair(it.latitude, it.longitude) })
            }
        }

        LocationManagerCompat.getCurrentLocation(
            manager,
            providers.first(),
            noCancel,
            ContextCompat.getMainExecutor(context),
            receiver
        )
    } catch (e: SecurityException) {
        onResult(null)
    }
}