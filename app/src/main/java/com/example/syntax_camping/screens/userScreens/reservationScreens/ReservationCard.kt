package com.example.syntax_camping.screens.userScreens.reservationScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syntax_camping.dataClasses.Reservation
import com.example.syntax_camping.dataClasses.ReservationRepository

/** Loads the reservation by id, then shows it. Use this in lists of reservation ids. */
@Composable
fun ReservationCard(
    reservationId: String,
    onClick: (Reservation) -> Unit,
    modifier: Modifier = Modifier
) {
    var res by remember(reservationId) { mutableStateOf<Reservation?>(null) }
    LaunchedEffect(reservationId) {
        ReservationRepository().getReservation(reservationId) { res = it }
    }
    ReservationCard(
        reservation = res,
        reservationId = reservationId,
        onClick = onClick,
        modifier = modifier
    )
}

/** Shows a reservation you already have (null shows "Loading..."). */
@Composable
fun ReservationCard(
    reservation: Reservation?,
    reservationId: String = reservation?.reservationId ?: "",
    onClick: (Reservation) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .testTag("reservationCard_$reservationId")
            .clickable(enabled = reservation != null) { reservation?.let(onClick) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4E8D5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = reservation?.campsite?.name?.ifBlank { "Campsite" } ?: "Loading...",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            if (reservation != null) {
                if (reservation.bookingName.isNotBlank()) {
                    Text(
                        "Booked under: ${reservation.bookingName}",
                        modifier = Modifier.testTag("reservationBookingName_$reservationId")
                    )
                }
                Text("Dates: ${reservation.datesReserved.firstOrNull() ?: ""} to ${reservation.datesReserved.lastOrNull() ?: ""}")
                Text("Guests: ${reservation.numberGuests}")
                Text("Status: ${reservation.status}")
            }
        }
    }
}