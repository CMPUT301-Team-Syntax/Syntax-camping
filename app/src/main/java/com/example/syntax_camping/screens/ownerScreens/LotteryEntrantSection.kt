package com.example.syntax_camping.screens.ownerScreens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syntax_camping.Database
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.LotteryEntry
import com.google.firebase.Timestamp

@Composable
fun LotteryEntrantSection(lottery: Lottery?, userId: String) {
    val lotteryKey = lottery?.lotteryId
    var checking by remember(lotteryKey) { mutableStateOf(true) }
    var inLottery by remember(lotteryKey) { mutableStateOf(false) }
    var busy by remember(lotteryKey) { mutableStateOf(false) }

    // Check if user is in lottery when screen opens
    LaunchedEffect(lotteryKey, userId) {
        if (lottery != null) {
            Database().hasUserJoinedLottery(lottery.lotteryId, userId) { joined ->
                inLottery = joined
                checking = false
            }
        } else {
            checking = false
        }
    }

    if (lottery != null && (lottery.status == "initialAccepting" || lottery.status == "booking")) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (inLottery) {
                Text("You're in now!", color = Color(0xFF3E6B68), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        busy = true
                        lottery.removeEntry(LotteryEntry(userId = userId, joined = Timestamp.now())) { left ->
                            if (left) inLottery = false
                            busy = false
                        }
                    },
                    enabled = !busy && !checking,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2C9C9))
                ) {
                    Text("Click to leave lottery", color = Color.Black)
                }
            } else {
                Button(
                    onClick = {
                        busy = true
                        lottery.addEntry(LotteryEntry(userId = userId, joined = Timestamp.now())) { joined ->
                            if (joined) inLottery = true
                            busy = false
                        }
                    },
                    enabled = !busy && !checking,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2C9C9))
                ) {
                    Text(if (lottery.status == "booking") "Click to join waitlist!!" else "Click to join lottery!!", color = Color.Black)
                }
            }
        }
    }
}