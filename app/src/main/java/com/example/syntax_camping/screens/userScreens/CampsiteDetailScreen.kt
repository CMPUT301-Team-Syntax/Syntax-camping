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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.Review
import com.example.syntax_camping.dataClasses.lotteryRepository
import com.example.syntax_camping.screens.CalenderVeiw
import com.example.syntax_camping.screens.CampsiteQRCode
import com.example.syntax_camping.screens.Rating
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.TopBar

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

    if (campsite == null) {
        return
    }

    var lotteryState by remember { mutableStateOf<Lottery?>(null) }

    // Real-time UI states for Rating
    var displayedRating by remember(campsite) { mutableDoubleStateOf(campsite.rating) }
    var displayedRaters by remember(campsite) { mutableIntStateOf(campsite.raters) }
    var showRatingInput by remember { mutableStateOf(false) }
    var showThankYouMessage by remember { mutableStateOf(false) }
    var showRatingPopup by remember { mutableStateOf(false) }
    var showDeleteReviewDialog by remember { mutableStateOf(false) }
    var userReviewString by remember { mutableStateOf("") }
    var userReviewTextString by remember { mutableStateOf("") }
    var ratingError by remember { mutableStateOf("") }
    val reviewDateFormat = remember { java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault()) }

    // CHANGED: reviews are shown as a list; this counter makes the list rebuild after a submit / edit / delete
    var reviewsVersion by remember { mutableIntStateOf(0) }
    val reviews = remember(reviewsVersion, campsite) { campsite.ratings.toList() }
    val myReview = reviews.find { it.userId == userViewModel.deviceId }

    val isOwner =
        userViewModel.currentUser?.ownerData?.campsiteIds?.contains(campsiteID ?: "") == true

    val nonBlankLotteryId = campsite.lotteryId?.takeIf { it.isNotBlank() }
    LaunchedEffect(nonBlankLotteryId) {
        if (nonBlankLotteryId != null) {
            lotteryFolder.getLottery(nonBlankLotteryId) { fetchedLottery ->
                lotteryState = fetchedLottery?.takeIf { !it.status.isNullOrBlank() }
            }
        } else {
            lotteryState = null
        }
    }

    val currentLottery = lotteryState

    val context = LocalContext.current
    var myWinnerStatus by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(nonBlankLotteryId) {
        if (nonBlankLotteryId != null) {
            lotteryFolder.getWinners(nonBlankLotteryId) { list ->
                myWinnerStatus = list.firstOrNull { it.userID == userViewModel.deviceId }?.status
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
                        displayedRating,
                        modifier = Modifier.testTag("Rating")
                    )
                    Text(
                        text = " $displayedRating ($displayedRaters)",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.testTag("ratingSummaryText")
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
                        CampsiteRulesLink(navController)
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
                        CancellationPolicyLink(navController)
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

                    if (currentLottery != null && currentLottery.lotteryDates.isNotEmpty()) {
                        Text(
                            text = "Dates offered by this lottery",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        CalenderVeiw(initialSelected = currentLottery.lotteryDates, readOnly = true)
                    }

                    if (!update && !isOwner && currentLottery != null &&
                        myWinnerStatus == "pending" &&
                        currentLottery.status != "complete" &&
                        System.currentTimeMillis() <= currentLottery.endTimeBooking &&
                        winnerAccepted(context, currentLottery.lotteryId, userViewModel.deviceId)
                    ) {
                        Button(
                            onClick = {
                                navController.navigate("bookCampsite/${campsite.documentId}/${currentLottery.lotteryId}")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                                .testTag("bookCampsiteButton"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68))
                        ) {
                            Text("Book this campsite", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // OWNER LOTTERY CONTROLS (join/leave now lives on the lottery info screen)
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
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3E6B68)
                                )
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
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3E6B68)
                                )
                            ) {
                                Text(
                                    "Edit Lottery",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } // end Lottery Section

                // ================== REVIEWS SECTION ==================
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Reviews", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                // Leave / edit form (non-owners only)
                if (!isOwner && !update) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (showThankYouMessage) {
                            Text(
                                text = "Thank you for leaving a rating",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3E6B68),
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .testTag("thankYouRatingText")
                            )
                        }

                        if (showRatingInput) {
                            OutlinedTextField(
                                value = userReviewString,
                                onValueChange = { userReviewString = it },
                                label = { Text("Leave a rating out of 5") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("campsite_name_input")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = userReviewTextString,
                                onValueChange = { userReviewTextString = it },
                                label = { Text("Write a review (optional)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reviewTextInput")
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val parsedRating = userReviewString.toDoubleOrNull()
                                    if (parsedRating != null) {
                                        if (parsedRating in 0.0..5.0) {
                                            val newReview = Review(
                                                parsedRating,
                                                userViewModel.deviceId,
                                                userReviewTextString.trim(),
                                                userViewModel.currentUser?.name ?: "",
                                                System.currentTimeMillis()
                                            )
                                            if (myReview == null) {
                                                campsite.updateRating(newReview)
                                            } else {
                                                campsite.replaceRating(newReview)
                                            }

                                            displayedRating = campsite.rating
                                            displayedRaters = campsite.raters
                                            reviewsVersion++
                                            ratingError = ""
                                            showRatingInput = false
                                            showThankYouMessage = true
                                            showRatingPopup = true
                                        } else {
                                            ratingError = "review must be between 0.0 and 5.0"
                                        }
                                    } else {
                                        ratingError =
                                            "you must enter a rating before you can review"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                                    .testTag("submitRatingButton"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3E6B68)
                                )
                            ) {
                                Text(
                                    "Submit Rating",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            TextButton(
                                onClick = {
                                    ratingError = ""
                                    showRatingInput = false
                                },
                                modifier = Modifier.testTag("cancelReviewButton")
                            ) {
                                Text("Cancel")
                            }

                            if (ratingError.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = ratingError,
                                    fontSize = 16.sp,
                                    color = Color(0xFFA13F3F),
                                    modifier = Modifier.testTag("ratingErrorText")
                                )
                            }
                        } else if (myReview == null) {
                            Button(
                                onClick = {
                                    ratingError = ""
                                    userReviewString = ""
                                    userReviewTextString = ""
                                    showRatingInput = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                                    .testTag("openRatingPrompt"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3E6B68)
                                )
                            ) {
                                Text(
                                    text = "Do you want to leave a rating?",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // List of reviews: yours first, then everyone else's
                if (reviews.isEmpty()) {
                    Text(
                        text = "No reviews yet",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.testTag("noReviewsText")
                    )
                } else {
                    reviews
                        .sortedByDescending { it.userId == userViewModel.deviceId }
                        .forEach { r ->
                            val isMine = r.userId == userViewModel.deviceId
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .background(Color.White, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                                    .testTag("reviewCard_${r.userId}")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = r.reviewerName.ifBlank { "Camper" } + (if (isMine) " (you)" else ""),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            modifier = Modifier.testTag("reviewerName_${r.userId}")
                                        )
                                        if (r.timestamp > 0L) {
                                            Text(
                                                text = reviewDateFormat.format(java.util.Date(r.timestamp)),
                                                fontSize = 12.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.testTag("reviewDate_${r.userId}")
                                            )
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Rating(
                                            r.rating,
                                            modifier = Modifier.testTag("reviewStars_${r.userId}")
                                        )
                                        Text(
                                            text = " ${r.rating}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                if (r.reviewText.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = r.reviewText,
                                        fontSize = 14.sp,
                                        modifier = Modifier.testTag("reviewText_${r.userId}")
                                    )
                                }
                                if (isMine && !showRatingInput) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = {
                                                ratingError = ""
                                                userReviewString = r.rating.toString()
                                                userReviewTextString = r.reviewText
                                                showRatingInput = true
                                            },
                                            modifier = Modifier.testTag("editRatingPrompt")
                                        ) {
                                            Text("Edit", color = Color(0xFF3E6B68))
                                        }
                                        TextButton(
                                            onClick = { showDeleteReviewDialog = true },
                                            modifier = Modifier.testTag("deleteReviewButton")
                                        ) {
                                            Text("Delete", color = Color(0xFFA13F3F))
                                        }
                                    }
                                }
                            }
                        }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // -- Campsite QR Code Section --
                if (campsite.hasQRCode) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Campsite QR Code",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        CampsiteQRCode(
                            campsiteId = campsite.documentId
                        )
                    }
                }
            } // end padded Column
        } // end scrolling Column

        // Rating Received Popup
        if (showRatingPopup) {
            AlertDialog(
                onDismissRequest = { showRatingPopup = false },
                modifier = Modifier.testTag("ratingPopup"),
                title = { Text(text = "Rating received") },
                text = { Text(text = "Thank you for leaving a rating") },
                confirmButton = {
                    TextButton(
                        onClick = { showRatingPopup = false },
                        modifier = Modifier.testTag("ratingPopupOk")
                    ) {
                        Text("OK")
                    }
                }
            )
        }

        // Delete review confirmation
        if (showDeleteReviewDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteReviewDialog = false },
                modifier = Modifier.testTag("deleteReviewDialog"),
                title = { Text(text = "Delete your review?") },
                text = { Text(text = "This removes your rating and review from this campsite.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            campsite.removeRating(userViewModel.deviceId)
                            displayedRating = campsite.rating
                            displayedRaters = campsite.raters
                            reviewsVersion++
                            showThankYouMessage = false
                            showDeleteReviewDialog = false
                        },
                        modifier = Modifier.testTag("confirmDeleteReviewButton")
                    ) {
                        Text("Delete", color = Color(0xFFA13F3F), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteReviewDialog = false },
                        modifier = Modifier.testTag("cancelDeleteReviewButton")
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}