package com.example.syntax_camping.UserScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
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
import com.example.syntax_camping.PhotoCarousel
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.CustomBottomBar
import com.example.syntax_camping.screens.Rating
import com.example.syntax_camping.screens.TopBar
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.LotteryEntry
import com.example.syntax_camping.dataClasses.lotteryRepository
import com.example.syntax_camping.screens.LotteryJoinState
import com.example.syntax_camping.screens.lotteryJoinState
import com.google.firebase.Timestamp
import getLotteryCounts

@Composable
fun CampsiteDetailsScreen(
    navController: NavController,
    campsiteID: String?,
    userViewModel: UserViewModel,
    update: Boolean = false,
    onEditCampsite: ((Campsite?) -> Unit)? = null,
    onEditLottery: ((Campsite?) -> Unit)? = null
) {
    val allCampsites = CampsiteRepository.getCampsites()
    val campsite = allCampsites.find { it.documentId == campsiteID }
    val campsiteCarousel = remember(campsite) { PhotoCarousel(campsite?.images ?: emptyList()) }
    val scrollState = rememberScrollState()
    val lotteryFolder = lotteryRepository()
    var lotteryState by remember { mutableStateOf<Lottery?>(null) }
    var userReviewString by remember { mutableStateOf("") }
    var userReview by remember { mutableStateOf(0.0) }
    val db = Database()
    var ratingError by remember{mutableStateOf("")}

    if (campsite == null) {
        return
    }

    val isOwner =
        userViewModel.currentUser?.ownerData?.campsiteIds?.contains(campsiteID ?: "") == true

    // Fetch the lottery details safely
    campsite.lotteryId?.takeIf { it.isNotBlank() }?.let { nonNullId ->
        LaunchedEffect(nonNullId) {
            lotteryFolder.getLottery(nonNullId) { fetchedLottery ->
                lotteryState = fetchedLottery
            }
        }
    }

    val currentLottery = lotteryState

    // 1. Get sizes cleanly using your helper function
    val (entryCount, waitlistCount) = getLotteryCounts(currentLottery)

    // 2. Track if the user has joined
    var hasJoined by remember { mutableStateOf(false) }

    // 3. Check if the user is in the lottery
    LaunchedEffect(currentLottery?.lotteryId) {
        currentLottery?.lotteryId?.takeIf { it.isNotBlank() }?.let { id ->
            db.hasUserJoinedLottery(id, userViewModel.deviceId) { joined ->
                hasJoined = joined
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4E8D5)),
        containerColor = Color(0xFFF4E8D5),
        topBar = {
            TopBar(navController)
        },
        bottomBar = {
            CustomBottomBar(
                navController = navController,
                currentRoute = "CampsiteDetails",
                backgroundColor = Color(0xFF3E6B68)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            if (update) {
                Button(
                    onClick = {
                        if (onEditCampsite != null) {
                            onEditCampsite(campsite)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68))
                ) {
                    Text("Update Campsite", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // -- Photo Carousel Section --
            campsiteCarousel.displayCarousel(
                modifier = Modifier.fillMaxWidth(),
                photoCarousel = campsiteCarousel
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Rating(campsite.rating)
                    Text(
                        text = " ${campsite.rating} (${campsite.raters})",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = campsite.name,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Left column
                    Column(
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.location_on),
                                contentDescription = "location",
                                tint = Color.Unspecified,
                            )
                            Text(
                                text = " ${campsite.location}",
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "💰 Rate: $25/n", fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "🏕️ Campsite Rules", fontSize = 16.sp, color = Color.Blue)
                    }

                    // Right column
                    Column(
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.occupancy),
                                contentDescription = "maxOccupancy",
                                tint = Color.Unspecified,
                            )
                            Text(text = " Max Occupancy: ${campsite.maxOcc}", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "📜 Cancellation policy", fontSize = 16.sp, color = Color.Blue)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
                Text(text = "Description", fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (campsite.description == "") "Description is empty" else campsite.description,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(40.dp))

                // Lottery Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (currentLottery == null) {
                        Text(
                            text = "No lotteries available",
                            color = Color(0xFFA13F3F),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = "Lottery Status: ",
                                color = Color(0xFFA13F3F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            currentLottery.status?.let {
                                Text(
                                    text = it,
                                    color = Color(0xFFA13F3F),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!update) {
                        // Blue Hyperlink
                        campsite.lotteryId?.takeIf { it.isNotBlank() }?.let { lotteryIdForLink ->
                            Text(
                                text = "Learn more about the lottery here",
                                color = Color.Blue,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                                fontSize = 14.sp,
                                modifier = Modifier.clickable {
                                    navController.navigate("LotteryDetailsScreen/$lotteryIdForLink")
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        val joinState = lotteryJoinState(currentLottery?.status)

                        if (isOwner) {
                            Text(
                                text = "You own this campsite. You cannot enter its lottery.",
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                        } else {
                            when (joinState) {
                                LotteryJoinState.ENDED -> {
                                    Text(
                                        "Lottery ended",
                                        color = Color(0xFFA13F3F),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }

                                LotteryJoinState.CAN_ENTER_LOTTERY, LotteryJoinState.CAN_JOIN_WAITLIST -> {
                                    if (hasJoined) {
                                        // LEAVE BUTTON
                                        Button(
                                            onClick = {
                                                campsite.lotteryId?.let { nonNullLotteryId ->
                                                    db.removeLotteryEntry(
                                                        nonNullLotteryId,
                                                        LotteryEntry(
                                                            userId = userViewModel.deviceId,
                                                            Timestamp.now()
                                                        )
                                                    ) { success ->
                                                        if (success) {
                                                            hasJoined = false
                                                        }
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Text(
                                                text = if (joinState == LotteryJoinState.CAN_JOIN_WAITLIST) "Leave Waitlist" else "Leave Lottery",
                                                color = Color.Black
                                            )
                                        }
                                    } else {
                                        // JOIN BUTTON
                                        Button(
                                            onClick = {
                                                campsite.lotteryId?.let { nonNullLotteryId ->
                                                    db.addLotteryEntry(
                                                        nonNullLotteryId,
                                                        LotteryEntry(
                                                            userId = userViewModel.deviceId,
                                                            Timestamp.now()
                                                        )
                                                    ) { success ->
                                                        if (success) {
                                                            hasJoined = true
                                                        }
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(
                                                    0xFFF2C9C9
                                                )
                                            ),
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Text(
                                                text = if (joinState == LotteryJoinState.CAN_JOIN_WAITLIST) "Click to join waitlist!!" else "Click to enter lottery!!",
                                                color = Color.Black,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // DYNAMIC TIMER & COUNTS
                                    val targetTime = if (currentLottery?.status == "booking") {
                                        currentLottery.endTimeBooking
                                    } else {
                                        currentLottery?.endTimeLottery ?: 0L
                                    }

                                    val daysLeft = if (targetTime > 0L) {
                                        ((targetTime - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).coerceAtLeast(
                                            0
                                        )
                                    } else {
                                        0
                                    }

                                    Text(
                                        text = "$waitlistCount people on waitlist\n$entryCount people in lottery\n$daysLeft days left",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }

                                LotteryJoinState.NONE -> {}
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                onEditLottery?.invoke(campsite)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68))
                        ) {
                            Text("Add Lottery", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = userReviewString,
                    onValueChange = { userReviewString = it },
                    label = { Text("Leave a review out of 5") },
                    modifier = Modifier.fillMaxWidth().testTag("campsite_name_input")
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if(userReviewString.toDoubleOrNull() != null){
                            userReview = userReviewString.toDoubleOrNull()!!
                            if((userReview >= 0.0) && (userReview <= 5.0)){
                                campsite.updateRating(userReview)
                                ratingError = ""
                            }

                            else{
                                ratingError = "review must be between 0.0 and 5.0"
                            }

                        }
                        else{
                            ratingError = "you must enter a rating before you can review"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68))
                ) {
                    Text("Leave Review", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
                if(ratingError!= "") {




                        Text(text = ratingError, fontSize = 16.sp)

                }
                //THIS WILL CHANGE WITH MAP IMPLEMENTATION SLIGHTLY AS IT NEEDS TO ALSO RECIEVE POTENTIAL STRINGS FORM THE MAP/TODO


            }
        }
    }
}