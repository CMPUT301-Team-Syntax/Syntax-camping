package com.example.syntax_camping.dataClasses

import com.google.firebase.Timestamp

/** WORK IN PROGRESS
 * Reservation data class that goes with Campsites.
 */
data class Reservation (
    val documentId: String,
    val time: Int,
    val location: String,
    val confirmed: Boolean,
) {}