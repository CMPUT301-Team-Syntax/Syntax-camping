package com.example.syntax_camping.UserScreens
import com.example.syntax_camping.screens.CalenderVeiw
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
import com.example.syntax_camping.dataClasses.lotteryRepository
import com.example.syntax_camping.screens.ownerScreens.lotteryScreens.lotteryList.LotteryCard
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.TopBar

@Composable
fun ReservationsScreen(navController: NavController, userViewModel: UserViewModel) {
    val db = remember { Database() }
    val repo = remember { lotteryRepository() }
    val registeredLotteries = remember { mutableStateListOf<Lottery>() }
    val currentReservations = remember { mutableStateListOf<String>() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userViewModel.deviceId) {
        isLoading = true
        userViewModel.refreshCurrentUser()

        val resIds = userViewModel.currentUser?.entrantData?.reservationIds ?: emptyList()
        currentReservations.clear()
        currentReservations.addAll(resIds)

        repo.getLotteries { allLotteries ->
            if (allLotteries.isEmpty()) {
                registeredLotteries.clear()
                isLoading = false
                return@getLotteries
            }

            val matched = mutableListOf<Lottery>()
            var checkedCount = 0

            allLotteries.forEach { lot ->
                db.hasUserJoinedLottery(lot.lotteryId, userViewModel.deviceId) { joined ->
                    if (joined) {
                        matched.add(lot)
                    }
                    checkedCount++
                    if (checkedCount == allLotteries.size) {
                        registeredLotteries.clear()
                        registeredLotteries.addAll(matched)
                        isLoading = false
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
            } else if (registeredLotteries.isEmpty() && currentReservations.isEmpty()) {
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
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF4E8D5))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Reservation: $reservationId",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}