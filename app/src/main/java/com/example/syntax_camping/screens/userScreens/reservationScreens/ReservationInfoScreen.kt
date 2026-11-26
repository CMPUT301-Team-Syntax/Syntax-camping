package com.example.syntax_camping.screens.userScreens.reservationScreens

import android.widget.Toast
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.syntax_camping.ui.components.PromptCsvDownload

@Composable
fun ReservationInfoScreen(
    navController: NavController,
    reservationId: String
) {
    val context = LocalContext.current
    val repo = remember { ReservationRepository() }
    var reservation by remember(reservationId) { mutableStateOf<Reservation?>(null) }
    var loaded by remember(reservationId) { mutableStateOf(false) }

    var menuExpanded by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }
    var cancelling by remember { mutableStateOf(false) }
    var cancelFailed by remember { mutableStateOf(false) }
    // set after a successful cancel so the user can download the cancellation CSV
    var csvToDownload by remember { mutableStateOf<String?>(null) }

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
                val isCancelled = r.status.trim().equals("cancelled", ignoreCase = true) ||
                        r.status.trim().equals("rejected", ignoreCase = true)
                val canCancel = !isCancelled && !r.status.trim().equals("past", ignoreCase = true) &&
                        !repo.hasEnded(r)
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
                                    text = { Text("View CSV") },
                                    modifier = Modifier.testTag("viewReservationCsvMenuItem"),
                                    onClick = {
                                        menuExpanded = false
                                        navController.navigate("reservationCsv/$reservationId")
                                    }
                                )
                                if (canCancel) {
                                    DropdownMenuItem(
                                        text = { Text("Cancel reservation") },
                                        modifier = Modifier.testTag("cancelReservationMenuItem"),
                                        onClick = {
                                            menuExpanded = false
                                            cancelReason = ""
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
                    if (isCancelled) {
                        InfoLine(
                            "Cancelled by",
                            if (r.cancelledBy == "owner") "Campsite owner" else "You",
                            "reservationInfoCancelledBy"
                        )
                        if (r.cancelReason.isNotBlank()) {
                            InfoLine("Reason", r.cancelReason, "reservationInfoReason")
                        }
                    }
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
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("This will cancel your stay at ${r.campsite.name.ifBlank { "this campsite" }}.")
                                OutlinedTextField(
                                    value = cancelReason,
                                    onValueChange = { cancelReason = it },
                                    label = { Text("Why are you cancelling?") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("cancelReasonInput")
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                enabled = !cancelling && cancelReason.isNotBlank(),
                                onClick = {
                                    cancelling = true
                                    repo.cancelReservation(
                                        reservationId = reservationId,
                                        reason = cancelReason.trim(),
                                        cancelledBy = "entrant"
                                    ) { success, csv ->
                                        cancelling = false
                                        showCancelDialog = false
                                        if (success) {
                                            // TODO notifications: tell the owner this person cancelled and why
                                            Toast.makeText(context, "Reservation cancelled", Toast.LENGTH_SHORT).show()
                                            csvToDownload = csv ?: repo.reservationCsv(r)
                                        } else {
                                            cancelFailed = true
                                        }
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

        if (cancelFailed) {
            AlertDialog(
                onDismissRequest = { cancelFailed = false },
                modifier = Modifier.testTag("cancelResultDialog"),
                title = { Text("Could not cancel") },
                text = { Text("Something went wrong. Please try again.") },
                confirmButton = {
                    TextButton(
                        onClick = { cancelFailed = false },
                        modifier = Modifier.testTag("cancelResultOk")
                    ) { Text("OK") }
                }
            )
        }

        // after a successful cancel: offer the CSV, then go back
        PromptCsvDownload(
            csvContent = csvToDownload,
            defaultFileName = "cancelled_reservation_$reservationId.csv",
            onDismiss = {
                csvToDownload = null
                navController.popBackStack()
            }
        )
    }
}

@Composable
private fun InfoLine(label: String, value: String, tag: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 16.sp, color = Color.Black, modifier = Modifier.testTag(tag))
    }
}