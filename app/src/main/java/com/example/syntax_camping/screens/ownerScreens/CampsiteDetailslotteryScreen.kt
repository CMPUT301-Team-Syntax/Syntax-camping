package com.example.syntax_camping.screens.ownerScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.lotteryRepository
import com.example.syntax_camping.screens.CampsiteCard
import com.example.syntax_camping.screens.CustomBottomBar
import com.example.syntax_camping.screens.TopBar
import com.example.syntax_camping.screens.ownerScreens.LotteryEntrantSection // Make sure this import matches where your file is!
import getLotteryCounts
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun LotteryDetailsScreen(navController: NavController, lotteryId: String?, userViewModel: UserViewModel, onEditLottery: (Lottery) -> Unit) {
    var lottery by remember { mutableStateOf<Lottery?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    // Fetch Lottery directly from your existing repository
    LaunchedEffect(lotteryId) {
        if (!lotteryId.isNullOrBlank()) {
            lotteryRepository().getLottery(lotteryId) { fetched ->
                lottery = fetched
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    // Countdown Timer Tick
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            now = System.currentTimeMillis()
        }
    }

    val current = lottery
    // Filter campsites currently held in app memory
    val participating = if (current == null) emptyList() else {
        CampsiteRepository.getCampsites().filter { it.lotteryId == current.lotteryId }
    }

    // Safely check if the user owns any of the participating campsites
    val ownedIds = userViewModel.currentUser?.ownerData?.campsiteIds ?: emptyList()
    val isOwner = participating.any { it.documentId in ownedIds }

    // Get sizes cleanly using your helper function
    val (entryCount, waitlistCount) = getLotteryCounts(current)

    Scaffold(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF4E8D5)),
        containerColor = Color(0xFFF4E8D5),
        topBar = { TopBar(navController) },
        bottomBar = { CustomBottomBar(navController, "LotteryDetails", Color(0xFF3E6B68)) }
    ) { innerPadding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (current == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) { Text("Lottery not found.") }
        } else {
            Column(Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())) {
                Column(Modifier.padding(24.dp)) {
                    Text(current.name.ifBlank { "Lottery" }, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    // Countdown Logic
                    val targetTime = if (current.status == "initialAccepting") current.endTimeLottery else current.endTimeBooking
                    if (current.status == "initialAccepting" || current.status == "booking") {
                        val millisLeft = targetTime - now
                        if (millisLeft > 0) {
                            Text("Closes in: ${formatRemaining(millisLeft)}", fontSize = 24.sp, color = Color(0xFF3E6B68), fontWeight = FontWeight.Bold)
                        } else {
                            Text("Time's up!", fontSize = 24.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("Draws: ${dateFormat.format(current.getReadableLotteryEndTime())}")
                    Text("Book by: ${dateFormat.format(current.getReadableBookingEndTime())}")
                    Text("Capacity: ${current.capacity?.toString() ?: "Unlimited"}")

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "$waitlistCount people on waitlist | $entryCount people in lottery",
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text("Participating Campsites", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(Modifier.height(8.dp))
                participating.forEach { camp ->
                    CampsiteCard(camp) {
                        navController.navigate(if (isOwner) "ownerCampsiteDetails/${camp.documentId}" else "campsiteDetails/${camp.documentId}")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Buttons (Owner vs Entrant)
                if (isOwner) {
                    if (current.status == "initialAccepting") {
                        Button(
                            onClick = { onEditLottery(current) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68))
                        ) {
                            Text("Edit lottery", color = Color.White)
                        }
                    } else {
                        Text("Lottery has been drawn and cannot be edited.", modifier = Modifier.padding(24.dp), color = Color.DarkGray)
                    }
                } else {
                    // YOUR SEPARATE FILE HANDLES ALL THE JOIN/LEAVE LOGIC HERE!
                    LotteryEntrantSection(
                        lottery = current,
                        userId = userViewModel.deviceId
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private fun formatRemaining(millisLeft: Long): String {
    val totalSecs = millisLeft / 1000
    val days = totalSecs / 86400
    val hours = (totalSecs % 86400) / 3600
    val mins = (totalSecs % 3600) / 60
    val secs = totalSecs % 60
    return if (days > 0) "${days}d ${hours}h ${mins}m ${secs}s" else "${hours}h ${mins}m ${secs}s"
}