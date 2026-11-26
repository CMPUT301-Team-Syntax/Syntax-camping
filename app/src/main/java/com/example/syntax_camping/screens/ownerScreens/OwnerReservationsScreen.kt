package com.example.syntax_camping.screens.ownerScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.dataClasses.Reservation
import com.example.syntax_camping.dataClasses.ReservationRepository
import com.example.syntax_camping.screens.CalenderVeiw
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.TopBar
import java.text.SimpleDateFormat
import java.util.Locale

private fun firstDateMillis(r: Reservation): Long {
    val first = r.datesReserved.firstOrNull() ?: return 0L
    return runCatching { SimpleDateFormat("dd/MM/yy", Locale.getDefault()).parse(first)?.time }
        .getOrNull() ?: 0L
}

/** Owner view: every reservation of one campsite, as cards titled with the booking person's name. */
@Composable
fun OwnerReservationsScreen(navController: NavController, campsiteId: String) {
    val repo = remember { ReservationRepository() }
    var campsite by remember(campsiteId) {
        mutableStateOf<Campsite?>(CampsiteRepository.getCampsites().find { it.documentId == campsiteId })
    }
    var reservations by remember(campsiteId) { mutableStateOf<List<Reservation>>(emptyList()) }
    var loaded by remember(campsiteId) { mutableStateOf(false) }

    // find the campsite (refresh the cache if it isn't there yet)
    LaunchedEffect(campsiteId) {
        if (campsite == null) {
            CampsiteRepository.refreshCampsiteCache {
                campsite = CampsiteRepository.getCampsites().find { it.documentId == campsiteId }
                if (campsite == null) loaded = true
            }
        }
    }
    // then load its reservations
    LaunchedEffect(campsite) {
        val c = campsite
        if (c != null) {
            repo.getAllReservationsForCampsite(c) {
                reservations = it
                loaded = true
            }
        }
    }

    val cancelled = reservations.filter {
        it.status.trim().equals("cancelled", true) || it.status.trim().equals("rejected", true)
    }.sortedByDescending { firstDateMillis(it) }
    val past = reservations.filter { it.status.trim().equals("past", true) }
        .sortedByDescending { firstDateMillis(it) }
    val active = reservations.filter { it !in cancelled && it !in past }
        .sortedBy { firstDateMillis(it) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("ownerReservationsScreen"),
        containerColor = Color.White,
        topBar = { TopBar(navController) },
        bottomBar = {
            CustomBottomBar(
                navController = navController,
                currentRoute = "CampsiteOwnerHome",
                backgroundColor = Color(0xFF3E6B68)
            )
        }
    ) { innerPadding ->
        when {
            !loaded -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(modifier = Modifier.testTag("ownerReservationsLoading")) }

            campsite == null -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text("Campsite not found", modifier = Modifier.testTag("ownerReservationsNotFound")) }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        campsite?.name?.ifBlank { "Campsite" } ?: "Campsite",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("ownerReservationsTitle")
                    )
                    Text("Reservations", fontSize = 14.sp, color = Color.Gray)
                }

                // booked dates (US 02.06.01)
                item { SectionTitle("Booked dates") }
                item {
                    val bookedDates = active.flatMap { it.datesReserved }.distinct()
                    if (bookedDates.isEmpty()) {
                        Text("No dates are booked.", color = Color.DarkGray)
                    } else {
                        CalenderVeiw(
                            initialSelected = bookedDates,
                            readOnly = true,
                            selectedColor = Color(0xFF2E7D32)
                        )
                    }
                }

                reservationSection("Active", active, "ownerNoActive", navController)
                reservationSection("Past", past, "ownerNoPast", navController)
                reservationSection("Cancelled", cancelled, "ownerNoCancelled", navController)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.reservationSection(
    title: String,
    list: List<Reservation>,
    emptyTag: String,
    navController: NavController
) {
    item { SectionTitle("$title (${list.size})") }
    if (list.isEmpty()) {
        item { Text("None.", color = Color.DarkGray, modifier = Modifier.testTag(emptyTag)) }
    } else {
        items(list, key = { "${title}_${it.reservationId}" }) { r ->
            OwnerReservationCard(r) { navController.navigate("ownerReservation/${r.reservationId}") }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 12.dp)
    )
}

/** Titled with the person who booked, not the campsite. */
@Composable
private fun OwnerReservationCard(r: Reservation, onClick: () -> Unit) {
    val cancelled = r.status.trim().equals("cancelled", true) || r.status.trim().equals("rejected", true)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("ownerReservationCard_${r.reservationId}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4E8D5))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                r.bookingName.ifBlank { "Guest" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("ownerReservationName_${r.reservationId}")
            )
            Text("Dates: ${r.datesReserved.firstOrNull() ?: ""} to ${r.datesReserved.lastOrNull() ?: ""}")
            Text("Guests: ${r.numberGuests}")
            Text("Status: ${r.status.ifBlank { "-" }}")
            if (cancelled) {
                Text("Cancelled by: ${if (r.cancelledBy == "owner") "You" else "Guest"}")
                if (r.cancelReason.isNotBlank()) Text("Reason: ${r.cancelReason}")
            }
        }
    }
}