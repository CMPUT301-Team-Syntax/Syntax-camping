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

/** Settings -> "View cancelled reservations": the user's cancelled reservations. Tap one to see its CSV. */
@Composable
fun CancelledReservationsScreen(navController: NavController, userViewModel: UserViewModel) {
    val repo = remember { ReservationRepository() }
    var items by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(userViewModel.deviceId) {
        repo.getCancelledReservationsForUser(userViewModel.deviceId) {
            items = it
            loaded = true
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("cancelledReservationsScreen"),
        containerColor = Color.White,
        topBar = { TopBar(navController) },
        bottomBar = {
            CustomBottomBar(
                navController = navController,
                currentRoute = "Settings",
                backgroundColor = Color(0xFF3E6B68)
            )
        }
    ) { innerPadding ->
        when {
            !loaded -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(modifier = Modifier.testTag("cancelledLoading")) }

            items.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text("No cancelled reservations", modifier = Modifier.testTag("cancelledEmpty")) }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Cancelled reservations",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("cancelledTitle")
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items, key = { it.reservationId }) { r ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .clickable { navController.navigate("reservationCsv/${r.reservationId}") }
                                .testTag("cancelledCard_${r.reservationId}"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF4E8D5))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    r.campsite.name.ifBlank { "Campsite" },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Dates: ${r.datesReserved.firstOrNull() ?: ""} to ${r.datesReserved.lastOrNull() ?: ""}")
                                Text("Cancelled by: ${if (r.cancelledBy == "owner") "Campsite owner" else "You"}")
                                if (r.cancelReason.isNotBlank()) Text("Reason: ${r.cancelReason}")
                            }
                        }
                    }
                }
            }
        }
    }
}