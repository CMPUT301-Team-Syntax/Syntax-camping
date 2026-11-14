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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.Database
import com.example.syntax_camping.PhotoCarousel
import com.example.syntax_camping.R
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.dataClasses.EntrantData
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.LotteryEntry
import com.example.syntax_camping.dataClasses.lotteryRepository
import com.example.syntax_camping.screens.Rating
import com.example.syntax_camping.screens.ownerScreens.lotteryScreens.lotteryList.LotteryJoinState
import com.example.syntax_camping.screens.ownerScreens.lotteryScreens.lotteryList.lotteryJoinState
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.TopBar
import com.google.firebase.Timestamp

@Composable
fun CampsiteDetailsScreen(
    navController: NavController,
    campsiteID: String?,
    userViewModel: UserViewModel,
    update: Boolean = false,
    onEditCampsite: ((Campsite?) -> Unit)? = null,
    onEditLottery: ((Campsite?) -> Unit)? = null,
    onEditExistingLottery: ((Lottery) -> Unit)? = null
) {
    val allCampsites = CampsiteRepository.getCampsites()
    val campsite = allCampsites.find { it.documentId == campsiteID }
    val campsiteCarousel = remember(campsite) { PhotoCarousel(campsite?.images ?: emptyList()) }
    val scrollState = rememberScrollState()
    val lotteryFolder = remember { lotteryRepository() }
    val db = remember { Database() }

    var lotteryState by remember { mutableStateOf<Lottery?>(null) }
    var entryCount by remember { mutableIntStateOf(0) }
    var waitlistCount by remember { mutableIntStateOf(0) }
    var hasJoined by remember { mutableStateOf(false) }
    var isActionLoading by remember { mutableStateOf(false) }
    var popupMessage by remember { mutableStateOf<String?>(null) }

    var userReviewString by remember { mutableStateOf("") }
    var userReview by remember { mutableStateOf(0.0) }
    var ratingError by remember { mutableStateOf("") }

    if (campsite == null) {
        return
    }

    val isOwner =
        userViewModel.currentUser?.ownerData?.campsiteIds?.contains(campsiteID ?: "") == true

    fun refreshLotteryDetails(id: String) {
        lotteryFolder.getLottery(id) { fetchedLottery ->
            lotteryState = fetchedLottery?.takeIf { !it.status.isNullOrBlank() }
        }
        lotteryFolder.getEntries(id) { entries ->
            entryCount = entries.size
        }
        lotteryFolder.getWaitlist(id) { waitlist ->
            waitlistCount = waitlist.size
        }
        db.hasUserJoinedLottery(id, userViewModel.deviceId) { joined ->
            hasJoined = joined
        }
    }

    val nonBlankLotteryId = campsite.lotteryId?.takeIf { it.isNotBlank() }
    LaunchedEffect(nonBlankLotteryId, userViewModel.deviceId) {
        if (nonBlankLotteryId != null) {
            refreshLotteryDetails(nonBlankLotteryId)
        } else {
            lotteryState = null
            entryCount = 0
            waitlistCount = 0
            hasJoined = false
        }
    }

    val currentLottery = lotteryState

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
            if (update || isOwner) {
                if (onEditCampsite != null) {
                    Button(
                        onClick = { onEditCampsite(campsite) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68))
                    ) {
                        Text("Update Campsite", color = Color.White, fontWeight = FontWeight.Bold)
                    }
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
                    Rating(
                        campsite.rating,
                        modifier = Modifier.testTag("Rating")
                    )
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
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
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
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
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

                    nonBlankLotteryId?.let { lotteryIdForLink ->
                        Text(
                            text = "Learn more about the lottery here",
                            color = Color.Blue,
                            textDecoration = TextDecoration.Underline,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .testTag("learnMoreLotteryLink")
                                .clickable {
                                    navController.navigate("LotteryDetailsScreen/$lotteryIdForLink")
                                }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // OWNER LOTTERY CONTROLS vs ENTRANT LOTTERY CONTROLS
                    if (update || isOwner) {
                        if (isOwner && !update) {
                            Text(
                                text = "You own this campsite. You cannot enter its lottery.",
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (currentLottery == null) {
                            Button(
                                onClick = { onEditLottery?.invoke(campsite) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp)
                                    .testTag("addLottery"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68))
                            ) {
                                Text(
                                    "Add Lottery",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (currentLottery.status == "initialAccepting") {
                            Button(
                                onClick = {
                                    if (onEditExistingLottery != null) {
                                        onEditExistingLottery(currentLottery)
                                    } else {
                                        navController.navigate("LotteryDetailsScreen/${currentLottery.lotteryId}")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp)
                                    .testTag("editLotteryButton"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68))
                            ) {
                                Text(
                                    "Edit Lottery",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        val joinState = lotteryJoinState(currentLottery?.status)

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
                                val currentActiveCount = if (joinState == LotteryJoinState.CAN_JOIN_WAITLIST) {
                                    waitlistCount
                                } else {
                                    entryCount
                                }
                                val cap = currentLottery?.capacity
                                val isAtCapacity = cap != null && currentActiveCount >= cap

                                if (isActionLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("lotteryActionLoading")
                                    )
                                } else if (hasJoined) {
                                    // LEAVE BUTTON
                                    Button(
                                        onClick = {
                                            nonBlankLotteryId?.let { nonNullLotteryId ->
                                                isActionLoading = true
                                                db.removeLotteryEntry(
                                                    nonNullLotteryId,
                                                    LotteryEntry(
                                                        userId = userViewModel.deviceId,
                                                        joined = Timestamp.now()
                                                    )
                                                ) { success ->
                                                    isActionLoading = false
                                                    if (success) {
                                                        hasJoined = false
                                                        popupMessage = "Leave Successful"
                                                        refreshLotteryDetails(nonNullLotteryId)

                                                        // Also sync user's entrantData in Firestore
                                                        userViewModel.currentUser?.let { u ->
                                                            val oldEntrant = u.entrantData ?: EntrantData()
                                                            val updatedUser = u.copy(
                                                                entrantData = oldEntrant.copy(
                                                                    lotteryEntryIds = (oldEntrant.lotteryEntryIds - nonNullLotteryId).toMutableList()
                                                                )
                                                            )
                                                            db.updateUser(userViewModel.deviceId, updatedUser) {
                                                                userViewModel.refreshCurrentUser()
                                                            }
                                                        }
                                                    } else {
                                                        popupMessage = "Unable to leave lottery."
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.testTag("leaveLotteryButton"),
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
                                            if (isAtCapacity) {
                                                popupMessage = "Lottery is at full capacity"
                                                return@Button
                                            }
                                            nonBlankLotteryId?.let { nonNullLotteryId ->
                                                isActionLoading = true
                                                db.addLotteryEntry(
                                                    nonNullLotteryId,
                                                    LotteryEntry(
                                                        userId = userViewModel.deviceId,
                                                        joined = Timestamp.now()
                                                    )
                                                ) { success ->
                                                    isActionLoading = false
                                                    if (success) {
                                                        hasJoined = true
                                                        popupMessage = "Join Successful"
                                                        refreshLotteryDetails(nonNullLotteryId)

                                                        // Also sync user's entrantData in Firestore
                                                        userViewModel.currentUser?.let { u ->
                                                            val oldEntrant = u.entrantData ?: EntrantData()
                                                            val updatedIds = (oldEntrant.lotteryEntryIds + nonNullLotteryId).distinct().toMutableList()
                                                            val updatedUser = u.copy(
                                                                entrantData = oldEntrant.copy(
                                                                    lotteryEntryIds = updatedIds
                                                                )
                                                            )
                                                            db.updateUser(userViewModel.deviceId, updatedUser) {
                                                                userViewModel.refreshCurrentUser()
                                                            }
                                                        }
                                                    } else {
                                                        popupMessage = "Lottery is at full capacity"
                                                        refreshLotteryDetails(nonNullLotteryId)
                                                    }
                                                }
                                            }
                                        },
                                        enabled = !isAtCapacity,
                                        modifier = Modifier.testTag("joinLotteryButton"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFF2C9C9)
                                        ),
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

                                Spacer(modifier = Modifier.height(16.dp))

                                // DYNAMIC TIMER & COUNTS
                                val targetTime = if (currentLottery?.status == "booking") {
                                    currentLottery.endTimeBooking
                                } else {
                                    currentLottery?.endTimeLottery ?: 0L
                                }

                                val daysLeft = if (targetTime > 0L) {
                                    ((targetTime - System.currentTimeMillis()) / (1000L * 60 * 60 * 24)).coerceAtLeast(0)
                                } else {
                                    0L
                                }

                                Text(
                                    text = "$waitlistCount people on waitlist\n$entryCount people in lottery\n$daysLeft days left",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.testTag("lotteryCountsAndTimer")
                                )
                            }

                            LotteryJoinState.NONE -> {}
                        }
                    }
                }

                // Only non-owners can leave a rating
                if (!isOwner && !update) {
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = userReviewString,
                        onValueChange = { userReviewString = it },
                        label = { Text("Leave a rating out of 5") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("campsite_name_input")
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (userReviewString.toDoubleOrNull() != null) {
                                userReview = userReviewString.toDoubleOrNull()!!
                                if ((userReview >= 0.0) && (userReview <= 5.0)) {
                                    campsite.updateRating(userReview)
                                    ratingError = ""
                                } else {
                                    ratingError = "review must be between 0.0 and 5.0"
                                }
                            } else {
                                ratingError = "you must enter a rating before you can review"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .testTag("leaveRatingButton"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68))
                    ) {
                        Text("Leave Rating", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    if (ratingError != "") {
                        Text(text = ratingError, fontSize = 16.sp)
                    }
                }
            }
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
}