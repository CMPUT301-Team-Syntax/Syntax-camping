package com.example.syntax_camping.screens.ownerScreens.lotteryScreens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.PendingConfirmation
import java.text.SimpleDateFormat
import java.util.Locale

/** Winner's side of the lottery info screen. status = their winner status ("pending", "confirmed", "rejected") or null if not a winner. */
@Composable
fun LotteryWinnerSection(
    lottery: Lottery,
    userId: String,
    status: String?,
    windowClosed: Boolean,
    onChanged: () -> Unit
) {
    var showDeclineConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("lotteryAccepts", Context.MODE_PRIVATE) }
    val acceptKey = "accepted_${lottery.lotteryId}_$userId"
    var accepted by remember { mutableStateOf(prefs.getBoolean(acceptKey, false)) }
    var showWinDialog by remember { mutableStateOf(!accepted) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    val isPending = status == "pending"
    val hasDeclined = status == "rejected"
    val hasBooked = status == "confirmed"
    if (!isPending && !hasDeclined && !hasBooked) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("winnerBanner"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when {
            hasBooked -> {
                Text("You have booked your spot!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E6B68))
                Text("Your reservation for this lottery is confirmed.")
            }
            hasDeclined -> {
                Text("You declined your spot in this lottery.", fontWeight = FontWeight.Bold, color = Color(0xFFA13F3F))
            }
            windowClosed -> {
                Text("The booking window has closed.", fontWeight = FontWeight.Bold, color = Color(0xFFA13F3F))
                Text("You did not book in time, so your spot has been released.")
            }
            !accepted -> {
                Text("You won!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E6B68))
                Text("Accept your spot to start booking, or decline to give it up.")
                Button(
                    onClick = { showWinDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68)),
                    modifier = Modifier.testTag("respondToWinButton")
                ) { Text("Respond", color = Color.White) }
            }
            else -> {
                Text("You won!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E6B68))
                Text("Pick ONE campsite below and book your dates before the booking window closes.")
            }
        }
    }
    Spacer(Modifier.height(16.dp))

    if (isPending && !accepted && !windowClosed && showWinDialog) {
        AlertDialog(
            onDismissRequest = { showWinDialog = false },
            modifier = Modifier.testTag("winDialog"),
            title = { Text("Congratulations!") },
            text = {
                Text(
                    "You are one of the ${lottery.numWinners} winners of ${lottery.name.ifBlank { "this lottery" }}. " +
                            "Accept to start booking before ${dateFormat.format(lottery.getReadableBookingEndTime())}, " +
                            "or decline to give your spot up."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWinDialog = false
                        prefs.edit().putBoolean(acceptKey, true).apply()
                        accepted = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68)),
                    modifier = Modifier.testTag("winAcceptButton")
                ) { Text("Accept", color = Color.White) }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showWinDialog = false
                        showDeclineConfirm = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA13F3F)),
                    modifier = Modifier.testTag("winDeclineButton")
                ) { Text("Decline", color = Color.White) }
            }
        )
    }

    if (showDeclineConfirm) {
        AlertDialog(
            onDismissRequest = { showDeclineConfirm = false },
            modifier = Modifier.testTag("declineConfirmDialog"),
            title = { Text("Give up your spot?") },
            text = { Text("You will lose your place in this lottery. It may go to someone on the waitlist.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeclineConfirm = false
                        // Luke's rejectReservation also clears pendingConfirmations and entrantPendingEntryIds
                        PendingConfirmation("", lottery.lotteryId, userId).rejectReservation { onChanged() }
                    },
                    modifier = Modifier.testTag("confirmDeclineButton")
                ) { Text("Yes, decline", color = Color(0xFFA13F3F), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeclineConfirm = false
                        showWinDialog = true
                    },
                    modifier = Modifier.testTag("keepSpotButton")
                ) { Text("Keep my spot") }
            }
        )
    }
}