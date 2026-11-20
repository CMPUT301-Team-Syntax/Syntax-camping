package com.example.syntax_camping.screens.ownerScreens.lotteryScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.syntax_camping.Database
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.lotteryRepository
import com.example.syntax_camping.screens.ownerScreens.ownerCampsiteList.CampsiteCardOwner
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.TopBar
import com.example.syntax_camping.screens.userScreens.userCampsiteList.CampsiteCard
import com.google.firebase.firestore.FirebaseFirestore
import getLotteryCounts
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun LotteryDetailsScreen(
    navController: NavController,
    lotteryId: String?,
    userViewModel: UserViewModel,
    onEditLottery: (Lottery) -> Unit
) {
    val db = remember { Database() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    var lottery by remember { mutableStateOf<Lottery?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    var campsitePendingLastRemoval by remember { mutableStateOf<Campsite?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    LaunchedEffect(lotteryId, refreshTrigger) {
        if (!lotteryId.isNullOrBlank()) {
            CampsiteRepository.refreshCampsiteCache {
                lotteryRepository().getLottery(lotteryId) { fetched ->
                    lottery = fetched
                    isLoading = false
                }
            }
        } else {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            now = System.currentTimeMillis()
        }
    }

    val current = lottery
    val participating = remember(current, refreshTrigger) {
        if (current == null) emptyList() else {
            val fromRepo = CampsiteRepository.getCampsites().filter { it.lotteryId == current.lotteryId }
            if (fromRepo.isNotEmpty()) fromRepo else current.campsites
        }
    }

    val ownedIds = userViewModel.currentUser?.ownerData?.campsiteIds ?: emptyList()
    val isOwner = participating.any { it.documentId in ownedIds }

    // CHANGED: counts are loaded here and reloaded whenever refreshTrigger changes
    var entryCount by remember { mutableIntStateOf(0) }
    var waitlistCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(lotteryId, refreshTrigger) {
        if (!lotteryId.isNullOrBlank()) {
            lotteryRepository().getEntries(lotteryId) { entries -> entryCount = entries.size }
            lotteryRepository().getWaitlist(lotteryId) { waitlist -> waitlistCount = waitlist.size }
        }
    }
    fun removeSingleCampsiteFromLottery(campToRemove: Campsite, activeLottery: Lottery) {
        campToRemove.updateLotteryId("")
        CampsiteRepository.getCampsites().find { it.documentId == campToRemove.documentId }?.updateLotteryId("")
        db.updateCampsiteLotteryId(campToRemove.documentId, "")

        val remainingCampsites = participating
            .filter { it.documentId != campToRemove.documentId }
            .toMutableList()

        activeLottery.campsites.clear()
        activeLottery.campsites.addAll(remainingCampsites)

        db.updateDrawTimer(
            endTimeLottery = activeLottery.endTimeLottery,
            endTimeBooking = activeLottery.endTimeBooking,
            numberOfWinners = activeLottery.numWinners,
            campsites = remainingCampsites,
            capacity = activeLottery.capacity,
            name = activeLottery.name,
            lotteryId = activeLottery.lotteryId
        )

        refreshTrigger++
    }

    fun removeLastCampsiteAndDeleteLottery(lastCamp: Campsite, activeLottery: Lottery) {
        lastCamp.updateLotteryId("")
        CampsiteRepository.getCampsites().find { it.documentId == lastCamp.documentId }?.updateLotteryId("")
        db.updateCampsiteLotteryId(lastCamp.documentId, "")

        firestore.collection("lotteries")
            .document(activeLottery.lotteryId)
            .delete()
            .addOnCompleteListener {
                CampsiteRepository.refreshCampsiteCache {
                    navController.popBackStack()
                }
            }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4E8D5)),
        containerColor = Color(0xFFF4E8D5),
        topBar = { TopBar(navController) },
        bottomBar = { CustomBottomBar(navController, "LotteryDetails", Color(0xFF3E6B68)) }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (current == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Lottery not found.")
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(
                        current.name.ifBlank { "Lottery" },
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))

                    val targetTime = if (current.status == "initialAccepting") current.endTimeLottery else current.endTimeBooking
                    if (current.status == "initialAccepting" || current.status == "booking") {
                        val millisLeft = targetTime - now
                        if (millisLeft > 0) {
                            Text(
                                "Closes in: ${formatRemaining(millisLeft)}",
                                fontSize = 24.sp,
                                color = Color(0xFF3E6B68),
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                "Time's up!",
                                fontSize = 24.sp,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
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

                Text(
                    "Participating Campsites",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(8.dp))

                participating.forEach { camp ->
                    if (isOwner) {
                        CampsiteCardOwner(
                            campsite = camp,
                            onClick = {
                                navController.navigate("ownerCampsiteDetails/${camp.documentId}")
                            },
                            onRemoveFromLotteryClick = if (current.status == "initialAccepting") {
                                {
                                    if (participating.size <= 1) {
                                        campsitePendingLastRemoval = camp
                                    } else {
                                        removeSingleCampsiteFromLottery(camp, current)
                                    }
                                }
                            } else null,
                            onUpdateLotteryClick = if (current.status == "initialAccepting") {
                                { onEditLottery(current) }
                            } else null
                        )
                    } else {
                        CampsiteCard(camp) {
                            navController.navigate("campsiteDetails/${camp.documentId}")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isOwner) {
                    if (current.status == "initialAccepting") {
                        Button(
                            onClick = { onEditLottery(current) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68))
                        ) {
                            Text("Edit lottery", color = Color.White)
                        }
                    } else {
                        Text(
                            "Lottery has been drawn and cannot be edited.",
                            modifier = Modifier.padding(24.dp),
                            color = Color.DarkGray
                        )
                    }
                } else {
                    LotteryEntrantSection(
                        lottery = current,
                        userId = userViewModel.deviceId,
                        userViewModel = userViewModel,
                        onChanged = { refreshTrigger++ }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        val pendingCamp = campsitePendingLastRemoval
        if (pendingCamp != null && current != null) {
            AlertDialog(
                onDismissRequest = { campsitePendingLastRemoval = null },
                modifier = Modifier.testTag("deleteLotteryWarningDialog"),
                title = { Text("Delete Current Lottery?") },
                text = {
                    Text(
                        "This is the only campsite in this lottery. Removing it will delete this current lottery. Do you want to continue?"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            campsitePendingLastRemoval = null
                            removeLastCampsiteAndDeleteLottery(pendingCamp, current)
                        },
                        modifier = Modifier.testTag("confirmDeleteLotteryButton")
                    ) {
                        Text("Delete Lottery", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { campsitePendingLastRemoval = null },
                        modifier = Modifier.testTag("cancelDeleteLotteryButton")
                    ) {
                        Text("Cancel")
                    }
                }
            )
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