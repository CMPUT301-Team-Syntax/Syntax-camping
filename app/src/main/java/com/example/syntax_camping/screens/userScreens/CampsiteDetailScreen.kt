package com.example.syntax_camping.UserScreens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import kotlin.time.Clock

@Composable
fun CampsiteDetailsScreen(navController: NavController,
                          campsiteID: String?,
                          userViewModel: UserViewModel,
                          update: Boolean = false,
                          onEditCampsite: ((Campsite?) -> Unit)? = null,
                          onEditLottery: ((Campsite?) -> Unit)? = null){

    val allCampsites = CampsiteRepository.getCampsites()
    val campsite = allCampsites.find { it.documentId == campsiteID }
    val campsiteCarousel = remember(campsite) { PhotoCarousel(campsite?.images ?: emptyList() ) }
    val scrollState = rememberScrollState()
    val lotteryFolder = lotteryRepository()
    var lotteryState by remember { mutableStateOf<Lottery?>(null) }
    if (campsite == null) {
        //maybe a launched effect then a campsite not here, after 5 seconds back to home
        return
    }

    campsite.lotteryId?.takeIf { it.isNotBlank() }?.let { nonNullId ->   // CHANGED: a blank id is treated as "no lottery", so getLottery("") is never called
        LaunchedEffect(nonNullId) {
            lotteryFolder.getLottery(nonNullId) { fetchedLottery ->
                // This is the callback! Firebase drops the result right here.
                lotteryState = fetchedLottery
            }
        }
    }
    val currentLottery = lotteryState
    val db = Database()

    Scaffold(

        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4E8D5)), // Set the background color to white
        containerColor = Color(0xFFF4E8D5),
        topBar = { // top bar with back button and notification icon
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

        //inside the scaffold, this would be the content of the screen, start editing screen content from in here

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState) // Makes the content scrollable, gemini prompted, doubt its necessary though
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
                 Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically) {
                        Rating(campsite.rating)
                        Text(
                            text = " ${campsite.rating} (${campsite.raters})", //rating
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                //campsite name
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

                // 2 grid information
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Left column
                    Column (
                        modifier = Modifier.weight(1f)
                            .padding(end = 8.dp)
                    ){
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top)
                        {
                            Icon(
                                painter = painterResource(id = R.drawable.location_on),
                                contentDescription = "location",
                                tint = Color.Unspecified,)

                            Text(text = " ${campsite.location}",
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }


                        Spacer(modifier = Modifier.height(8.dp))


                        Text(text = "💰 Rate: $25/n",
                            fontSize = 16.sp,
                        ) // Hardcoded price placeholder

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "🏕️ Campsite Rules",
                            fontSize = 16.sp,
                            color = Color.Blue,
                        )

                    }


                    // Right column
                    Column(
                        modifier = Modifier.weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically)
                        {
                            Icon(
                                painter = painterResource(id = R.drawable.occupancy),
                                contentDescription = "maxOccupancy",
                                tint = Color.Unspecified,)

                            Text(text = " Max Occupancy: ${campsite.maxOcc}",
                                fontSize = 16.sp,
                            )

                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "📜 Cancellation policy",
                            fontSize = 16.sp,
                            color = Color.Blue
                        )
                    }
                }


                Spacer(modifier = Modifier.height(30.dp))

                // description dection
                Text(
                    text = "Description",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (campsite.description == "") {
                            "Description is empty"
                        } else {
                            campsite.description
                        },
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Lottery Waitlist Section
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
                        // Kotlin smart-casts currentLottery to non-null here!
                        Row( horizontalArrangement = Arrangement.SpaceBetween ) {

                            Text(
                                text = "Lottery Status: ",
                                color = Color(0xFFA13F3F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            currentLottery.status?.let {
                                Text(
                                    text = it, // Fixed: No error, no !!
                                    color = Color(0xFFA13F3F),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                        Spacer(modifier = Modifier.height(8.dp))

                    if (!update) {
                        // CHANGED: the lottery's status decides what the visitor sees (replaces the never-set isAvailable)
                        val joinState = lotteryJoinState(currentLottery?.status)
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
                                Button(
                                    onClick = { campsite.lotteryId?.let { nonNullLotteryId ->
                                        db.addLotteryEntry(lotteryId = nonNullLotteryId, LotteryEntry(userId = userViewModel.deviceId, Timestamp.now())) }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFF2C9C9) // button color
                                    ),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        // CHANGED: text depends on the state
                                        text = if (joinState == LotteryJoinState.CAN_JOIN_WAITLIST) "Click to join waitlist!!" else "Click to enter lottery!!",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "50+ people currently waiting\n5 days left", // unchanged placeholder
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                            LotteryJoinState.NONE -> { }   // no lottery or unknown status: nothing extra
                        }
                    }

                    else {

                        Button(
                            onClick = {
                                if (onEditLottery != null) {
                                    onEditLottery(campsite)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68))
                        ) {
                            Text("Update Lottery", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                }

                Spacer(modifier = Modifier.height(24.dp)) // Extra padding at the bottom
            }
        }

}

