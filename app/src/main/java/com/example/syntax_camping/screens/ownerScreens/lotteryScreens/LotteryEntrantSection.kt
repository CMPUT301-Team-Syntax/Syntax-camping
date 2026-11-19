package com.example.syntax_camping.screens.ownerScreens.lotteryScreens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.syntax_camping.Database
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.LotteryEntry
import com.example.syntax_camping.dataClasses.lotteryRepository
import com.example.syntax_camping.screens.ownerScreens.lotteryScreens.lotteryList.LotteryJoinState
import com.example.syntax_camping.screens.ownerScreens.lotteryScreens.lotteryList.lotteryJoinState
import com.google.firebase.Timestamp

@Composable
fun LotteryEntrantSection(
    lottery: Lottery,
    userId: String
) {
    val db = remember { Database() }
    val repo = remember { lotteryRepository() }

    var hasJoined by remember { mutableStateOf(false) }
    var isActionLoading by remember { mutableStateOf(false) }
    var popupMessage by remember { mutableStateOf<String?>(null) }
    var entryCount by remember { mutableIntStateOf(0) }
    var waitlistCount by remember { mutableIntStateOf(0) }

    fun refreshStatus() {
        val id = lottery.lotteryId
        if (id.isNotBlank()) {
            db.hasUserJoinedLottery(id, userId) { joined ->
                hasJoined = joined
            }
            repo.getEntries(id) { entries ->
                entryCount = entries.size
            }
            repo.getWaitlist(id) { waitlist ->
                waitlistCount = waitlist.size
            }
        }
    }

    LaunchedEffect(lottery.lotteryId, userId) {
        refreshStatus()
    }

    val joinState = lotteryJoinState(lottery.status)
    val currentActiveCount = if (joinState == LotteryJoinState.CAN_JOIN_WAITLIST) waitlistCount else entryCount
    val isAtCapacity = lottery.capacity != null && currentActiveCount >= lottery.capacity!!

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (joinState) {
            LotteryJoinState.ENDED -> {
                Text(
                    text = "Lottery ended",
                    color = Color(0xFFA13F3F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            LotteryJoinState.CAN_ENTER_LOTTERY, LotteryJoinState.CAN_JOIN_WAITLIST -> {
                if (isActionLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("lotteryActionLoading")
                    )
                } else if (hasJoined) {
                    Button(
                        onClick = {
                            isActionLoading = true
                            db.removeLotteryEntry(
                                lottery.lotteryId,
                                LotteryEntry(userId = userId, joined = Timestamp.now())
                            ) { success ->
                                isActionLoading = false
                                if (success) {
                                    hasJoined = false
                                    popupMessage = "Leave Successful"
                                    refreshStatus()
                                } else {
                                    popupMessage = "Unable to leave lottery."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("leaveLotteryButton"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = if (joinState == LotteryJoinState.CAN_JOIN_WAITLIST) "Leave Waitlist" else "Leave Lottery",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            if (isAtCapacity) {
                                popupMessage = "Lottery is at full capacity"
                                return@Button
                            }
                            isActionLoading = true
                            db.addLotteryEntry(
                                lottery.lotteryId,
                                LotteryEntry(userId = userId, joined = Timestamp.now())
                            ) { success ->
                                isActionLoading = false
                                if (success) {
                                    hasJoined = true
                                    popupMessage = "Join Successful"
                                    refreshStatus()
                                } else {
                                    popupMessage = "Lottery is at full capacity"
                                    refreshStatus()
                                }
                            }
                        },
                        enabled = !isAtCapacity,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("joinLotteryButton"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2C9C9)),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = when {
                                isAtCapacity -> "Lottery Full"
                                joinState == LotteryJoinState.CAN_JOIN_WAITLIST -> "Click to join waitlist!!"
                                else -> "Click to enter lottery!!"
                            },
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            LotteryJoinState.NONE -> {}
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    if (popupMessage != null) {
        AlertDialog(
            onDismissRequest = { popupMessage = null },
            modifier = Modifier.testTag("lotteryStatusPopup"),
            title = { Text(text = popupMessage!!) },
            text = { Text(text = popupMessage!!) },
            confirmButton = {
                TextButton(
                    onClick = { popupMessage = null },
                    modifier = Modifier.testTag("lotteryStatusPopupOk")
                ) {
                    Text("OK")
                }
            }
        )
    }
}