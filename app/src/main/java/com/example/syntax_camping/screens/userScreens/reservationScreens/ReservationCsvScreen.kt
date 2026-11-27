package com.example.syntax_camping.screens.userScreens.reservationScreens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.example.syntax_camping.dataClasses.Reservation
import com.example.syntax_camping.dataClasses.ReservationRepository

@Composable
fun ReservationCsvScreen(navController: NavController, reservationId: String) {
    val repo = remember { ReservationRepository() }
    var reservation by remember(reservationId) { mutableStateOf<Reservation?>(null) }
    var loaded by remember(reservationId) { mutableStateOf(false) }

    LaunchedEffect(reservationId) {
        repo.getReservation(reservationId) {
            reservation = it
            loaded = true
        }
    }

    val r = reservation
    val cancelled = r != null &&
            (r.status.equals("cancelled", true) || r.status.equals("rejected", true))

    CsvViewerScreen(
        navController = navController,
        title = if (cancelled) "Cancelled reservation" else "Reservation CSV",
        csv = r?.let { repo.reservationCsv(it) },
        loaded = loaded,
        fileName = (if (cancelled) "cancelled_reservation_" else "reservation_") + "$reservationId.csv",
        details = if (r == null) emptyList() else buildList {
            add("Campsite" to r.campsite.name.ifBlank { "Campsite" })
            if (r.bookingName.isNotBlank()) add("Booked under" to r.bookingName)
            add("Status" to r.status.ifBlank { "-" })
            if (cancelled) {
                add("Cancelled by" to if (r.cancelledBy == "owner") "Campsite owner" else "You")
                if (r.cancelReason.isNotBlank()) add("Reason" to r.cancelReason)
            }
        },
        notFoundText = "Reservation not found"
    )
}