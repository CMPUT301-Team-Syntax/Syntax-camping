package com.example.syntax_camping.screens.ownerScreens

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

/** Owner view of one reservation: shows the booking person and lets the owner cancel it with a reason. */
@Composable
fun OwnerReservationInfoScreen(navController: NavController, reservationId: String) {
    val context = LocalContext.current
    val repo = remember { ReservationRepository() }
    var reservation by remember(reservationId) { mutableStateOf<Reservation?>(null) }
    var loaded by remember(reservationId) { mutableStateOf(false) }

    var menuExpanded by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }
    var cancelling by remember { mutableStateOf(false) }
    var cancelFailed by remember { mutableStateOf(false) }

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
            .testTag("ownerReservationInfoScreen"),
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
        val r = reservation
        when {
            !loaded -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(modifier = Modifier.testTag("ownerReservationInfoLoading")) }

            r == null -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text("Reservation not found", modifier = Modifier.testTag("ownerReservationInfoNotFound")) }

            else -> {
                val isCancelled = r.status.trim().equals("cancelled", true) ||
                        r.status.trim().equals("rejected", true)
                val canCancel = !isCancelled && !r.status.trim().equals("past", true)

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
                        Column(Modifier.weight(1f)) {
                            Text(
                                r.bookingName.ifBlank { "Guest" },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("ownerReservationInfoName")
                            )
                            Text(r.campsite.name.ifBlank { "Campsite" }, fontSize = 14.sp, color = Color.Gray)
                        }
                        if (canCancel) {
                            Box {
                                IconButton(
                                    onClick = { menuExpanded = true },
                                    modifier = Modifier.testTag("ownerReservationInfoMenu")
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
                                        modifier = Modifier.testTag("ownerCancelReservationMenuItem"),
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

                    InfoLine("Status", r.status.ifBlank { "-" })
                    if (isCancelled) {
                        InfoLine("Cancelled by", if (r.cancelledBy == "owner") "You" else "Guest")
                        if (r.cancelReason.isNotBlank()) InfoLine("Reason", r.cancelReason)
                    }
                    InfoLine("Guests", r.numberGuests.toString())
                    InfoLine(
                        "Dates",
                        "${r.datesReserved.firstOrNull() ?: ""} to ${r.datesReserved.lastOrNull() ?: ""}" +
                                "  (${r.datesReserved.size} day(s))"
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
                        modifier = Modifier.testTag("ownerCancelDialog"),
                        title = { Text("Cancel this reservation?") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("${r.bookingName.ifBlank { "The guest" }} will no longer have this stay.")
                                OutlinedTextField(
                                    value = cancelReason,
                                    onValueChange = { cancelReason = it },
                                    label = { Text("Why are you cancelling?") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ownerCancelReasonInput")
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                enabled = !cancelling && cancelReason.isNotBlank(),
                                onClick = {
                                    cancelling = true
                                    r.rejectByOwner(
                                        rejectedStatus = "cancelled",
                                        reason = cancelReason.trim()
                                    ) { success, _ ->
                                        cancelling = false
                                        showCancelDialog = false
                                        if (success) {
                                            // TODO notifications: tell the guest the owner cancelled and why
                                            Toast.makeText(context, "Reservation cancelled", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        } else {
                                            cancelFailed = true
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("ownerConfirmCancel")
                            ) { Text("Yes", color = Color(0xFFA13F3F), fontWeight = FontWeight.Bold) }
                        },
                        dismissButton = {
                            TextButton(
                                enabled = !cancelling,
                                onClick = { showCancelDialog = false },
                                modifier = Modifier.testTag("ownerDismissCancel")
                            ) { Text("No") }
                        }
                    )
                }
            }
        }

        if (cancelFailed) {
            AlertDialog(
                onDismissRequest = { cancelFailed = false },
                title = { Text("Could not cancel") },
                text = { Text("Something went wrong. Please try again.") },
                confirmButton = {
                    TextButton(onClick = { cancelFailed = false }) { Text("OK") }
                }
            )
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 16.sp, color = Color.Black)
    }
}