package com.example.syntax_camping.screens.userScreens.reservationScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.syntax_camping.R
import com.example.syntax_camping.dataClasses.Reservation
import com.example.syntax_camping.dataClasses.ReservationRepository
import com.example.syntax_camping.screens.CalenderVeiw
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.TopBar
import com.example.syntax_camping.screens.userScreens.userCampsiteList.CampsiteCard

@Composable
fun ReservationInfoScreen(
    navController: NavController,
    reservationId: String
) {
    val repo = remember { ReservationRepository() }
    var reservation by remember(reservationId) { mutableStateOf<Reservation?>(null) }
    var loaded by remember(reservationId) { mutableStateOf(false) }

    var menuExpanded by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelling by remember { mutableStateOf(false) }
    // null = no result yet, true = cancelled, false = failed
    var cancelResult by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(reservationId) {
        repo.getReservation(reservationId) {
            reservation = it
            loaded = true
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("reservationInfoScreen"),
        containerColor = Color.White,
        topBar = { TopBar(navController) },
        bottomBar = {
            CustomBottomBar(
                navController = navController,
                currentRoute = "Reservations",
                backgroundColor = Color(0xFF3E6B68)
            )
        }
    ) { innerPadding ->
        val r = reservation
        when {
            !loaded -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(modifier = Modifier.testTag("reservationInfoLoading")) }

            r == null -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Reservation not found", modifier = Modifier.testTag("reservationInfoNotFound"))
            }

            else -> {
                val canCancel = !r.status.trim().equals("past", ignoreCase = true) &&
                        !r.status.trim().equals("cancelled", ignoreCase = true)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Reservation details",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("reservationInfoTitle")
                        )

                        if (canCancel) {
                            Box {
                                IconButton(
                                    onClick = { menuExpanded = true },
                                    modifier = Modifier.testTag("reservationInfoMenu")
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.more_vertical),
                                        contentDescription = "Options Menu",
                                        tint = Color.Unspecified
                                    )
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Cancel reservation") },
                                        modifier = Modifier.testTag("cancelReservationMenuItem"),
                                        onClick = {
                                            menuExpanded = false
                                            showCancelDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // the campsite card: tap it to go to the campsite's details page
                    CampsiteCard(r.campsite) {
                        navController.navigate("campsiteDetails/${r.campsite.documentId}")
                    }

                    if (r.bookingName.isNotBlank()) {
                        InfoLine("Booked under", r.bookingName, "reservationInfoBookingName")
                    }
                    InfoLine("Status", r.status.ifBlank { "-" }, "reservationInfoStatus")
                    InfoLine("Guests", r.numberGuests.toString(), "reservationInfoGuests")
                    InfoLine(
                        "Dates",
                        "${r.datesReserved.firstOrNull() ?: ""} to ${r.datesReserved.lastOrNull() ?: ""}" +
                                "  (${r.datesReserved.size} day(s))",
                        "reservationInfoDates"
                    )

                    if (r.datesReserved.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        CalenderVeiw(
                            initialSelected = r.datesReserved,
                            readOnly = true,
                            selectedColor = Color(0xFF2E7D32)
                        )
                    }
                }

                if (showCancelDialog) {
                    AlertDialog(
                        onDismissRequest = { if (!cancelling) showCancelDialog = false },
                        modifier = Modifier.testTag("cancelReservationDialog"),
                        title = { Text("Cancel reservation?") },
                        text = { Text("This will cancel your stay at ${r.campsite.name.ifBlank { "this campsite" }}.") },
                        confirmButton = {
                            TextButton(
                                enabled = !cancelling,
                                onClick = {
                                    cancelling = true
                                    repo.cancelReservation(reservationId) { success, _ ->
                                        cancelling = false
                                        showCancelDialog = false
                                        cancelResult = success
                                    }
                                },
                                modifier = Modifier.testTag("confirmCancelReservation")
                            ) { Text("Yes", color = Color(0xFFA13F3F), fontWeight = FontWeight.Bold) }
                        },
                        dismissButton = {
                            TextButton(
                                enabled = !cancelling,
                                onClick = { showCancelDialog = false },
                                modifier = Modifier.testTag("dismissCancelReservation")
                            ) { Text("No") }
                        }
                    )
                }
            }
        }

        cancelResult?.let { success ->
            AlertDialog(
                onDismissRequest = {
                    cancelResult = null
                    if (success) navController.popBackStack()
                },
                modifier = Modifier.testTag("cancelResultDialog"),
                title = { Text(if (success) "Reservation cancelled" else "Could not cancel") },
                text = {
                    Text(
                        if (success) "Your reservation has been cancelled."
                        else "Something went wrong. Please try again."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            cancelResult = null
                            if (success) navController.popBackStack()
                        },
                        modifier = Modifier.testTag("cancelResultOk")
                    ) { Text("OK") }
                }
            )
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String, tag: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 16.sp, color = Color.Black, modifier = Modifier.testTag(tag))
    }
}