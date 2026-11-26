package com.example.syntax_camping.UserScreens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.syntax_camping.Database
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.ReservationRepository
import com.example.syntax_camping.dataClasses.lotteryRepository
import com.example.syntax_camping.screens.ownerScreens.lotteryScreens.lotteryList.LotteryCard
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.TopBar
import com.example.syntax_camping.dataClasses.Reservation
import com.example.syntax_camping.screens.userScreens.reservationScreens.ReservationCard

@Composable
fun ReservationsScreen(navController: NavController, userViewModel: UserViewModel) {
    val db = remember { Database() }
    val repo = remember { lotteryRepository() }
    val registeredLotteries = remember { mutableStateListOf<Lottery>() }
    val wonLotteries = remember { mutableStateListOf<Lottery>() }
    val currentReservations = remember { mutableStateListOf<String>() }
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(userViewModel.currentUser) {
        currentReservations.clear()
        currentReservations.addAll(userViewModel.currentUser?.entrantData?.reservationIds ?: emptyList())
    }

    LaunchedEffect(userViewModel.deviceId) {
        isLoading = true
        userViewModel.refreshCurrentUser()
        repo.getLotteries { allLotteries ->
            if (allLotteries.isEmpty()) {
                registeredLotteries.clear()
                wonLotteries.clear()
                isLoading = false
                return@getLotteries
            }
            val matched = mutableListOf<Lottery>()
            val won = mutableListOf<Lottery>()
            var checkedCount = 0

            fun finishIfDone() {
                checkedCount++
                if (checkedCount == allLotteries.size) {
                    registeredLotteries.clear()
                    registeredLotteries.addAll(matched)
                    wonLotteries.clear()
                    wonLotteries.addAll(won)
                    isLoading = false
                }
            }

            allLotteries.forEach { lot ->
                db.hasUserJoinedLottery(lot.lotteryId, userViewModel.deviceId) { joined ->
                    if (!joined) {
                        finishIfDone()
                    } else {
                        repo.getWinners(lot.lotteryId) { winners ->
                            val myStatus = winners.firstOrNull { it.userID == userViewModel.deviceId }?.status
                            when (myStatus) {
                                "pending" -> won.add(lot)
                                "confirmed", "rejected" -> { }
                                else -> matched.add(lot)
                            }
                            finishIfDone()
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("reservationsScreen"),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Button for Past Reservations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { navController.navigate("PastReservations") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pastReservationsButton"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68))
                ) {
                    Text(
                        text = "Past Reservations",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.testTag("reservationsLoading"))
                }
            } else if (registeredLotteries.isEmpty() && wonLotteries.isEmpty() && currentReservations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No reservations and no lotteries joined.",
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Find a lottery to join?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3E6B68),
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .testTag("findLotteryToJoinLink")
                                .clickable {
                                    navController.navigate("Search")
                                }
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    if (wonLotteries.isNotEmpty()) {
                        Text(
                            text = "Your Won Lotteries",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3E6B68)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wonLotteriesSection")
                        ) {
                            wonLotteries.forEach { lottery ->
                                LotteryCard(
                                    lottery = lottery,
                                    onClick = {
                                        navController.navigate("LotteryDetailsScreen/${lottery.lotteryId}")
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Section 1: Registered Lotteries
                    Text(
                        text = "Your Registered Lotteries",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    if (registeredLotteries.isEmpty()) {
                        Text(
                            text = "No registered lotteries.",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("registeredLotteriesSection")
                        ) {
                            registeredLotteries.forEach { lottery ->
                                LotteryCard(
                                    lottery = lottery,
                                    onClick = {
                                        navController.navigate("LotteryDetailsScreen/${lottery.lotteryId}")
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Section 2: Current Reservations


                    Text(
                        text = "Your Current Reservations",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    if (currentReservations.isEmpty()) {
                        Text(
                            text = "No current reservations.",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("currentReservationsSection"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currentReservations.forEach { reservationId ->
                                ReservationCard(
                                    reservationId = reservationId,
                                    onClick = { navController.navigate("reservationInfo/$reservationId") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}