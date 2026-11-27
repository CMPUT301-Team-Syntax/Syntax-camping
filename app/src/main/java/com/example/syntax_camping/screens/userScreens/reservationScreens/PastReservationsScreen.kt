package com.example.syntax_camping.screens.userScreens.reservationScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.dataClasses.Reservation
import com.example.syntax_camping.dataClasses.ReservationRepository
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.TopBar
import java.text.SimpleDateFormat
import java.util.Locale

private fun lastDateMillis(r: Reservation): Long {
    val last = r.datesReserved.lastOrNull() ?: return 0L
    return runCatching { SimpleDateFormat("dd/MM/yy", Locale.getDefault()).parse(last)?.time }
        .getOrNull() ?: 0L
}

@Composable
fun PastReservationsScreen(
    navController: NavController,
    userViewModel: UserViewModel
) {
    val repo = remember { ReservationRepository() }
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(userViewModel.deviceId) {
        repo.getCompletedReservationsForUser(userViewModel.deviceId) { list ->
            reservations = list.sortedByDescending { lastDateMillis(it) }   // most recent first
            loaded = true
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("pastReservationsScreen"),
        containerColor = Color.White,
        topBar = {
            TopBar(navController)
        },
        bottomBar = {
            CustomBottomBar(
                navController = navController,
                currentRoute = "Reservations",
                backgroundColor = Color(0xFF3E6B68)
            )
        }
    ) { innerPadding ->
        when {
            !loaded -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(modifier = Modifier.testTag("pastReservationsLoading")) }

            reservations.isEmpty() -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Past Reservations",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "No past reservations found.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .testTag("noPastReservationsText")
                )
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Past Reservations",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(reservations, key = { it.reservationId }) { r ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("reservationInfo/${r.reservationId}") }
                                .testTag("pastReservationCard_${r.reservationId}"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF4E8D5))
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    r.campsite.name.ifBlank { "Campsite" },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (r.bookingName.isNotBlank()) Text("Booked under: ${r.bookingName}")
                                Text("Dates: ${r.datesReserved.firstOrNull() ?: ""} to ${r.datesReserved.lastOrNull() ?: ""}")
                                Text("Guests: ${r.numberGuests}")
                            }
                        }
                    }
                }
            }
        }
    }
}