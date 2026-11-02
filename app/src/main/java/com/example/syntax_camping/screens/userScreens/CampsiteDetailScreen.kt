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
import androidx.compose.runtime.remember
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
import com.example.syntax_camping.PhotoCarousel
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.Rating
import com.example.syntax_camping.screens.TopBar

@Composable
fun CampsiteDetailsScreen(navController: NavController, campsiteID: String?){

    val allCampsites = CampsiteRepository.getCampsites()
    val campsite = allCampsites.find { it.documentId == campsiteID }
    val campsiteCarousel = remember(campsite) { PhotoCarousel(campsite?.images ?: emptyList() ) }
    val scrollState = rememberScrollState()

    if (campsite == null) {
        // Optionally, put a Text("Campsite not found") here
        return
    }

    Scaffold(

        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4E8D5)), // Set the background color to white
        containerColor = Color(0xFFF4E8D5),
        topBar = { // top bar with back button and notification icon
            TopBar(navController)
        },

        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFF3E6B68))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = { /* TODO: Navigate to Search */ },
                    modifier = Modifier.size(100.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Search",
                        tint = Color(0xFF3F6B68)
                    )
                }

                IconButton(onClick = { /* TODO: Navigate to Reservations */ },
                    modifier = Modifier.size(100.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.reservations),
                        contentDescription = "Reservations",
                        tint = Color.Unspecified
                    )
                }

                IconButton(onClick = { /* TODO: Navigate to Settings */ },
                    modifier = Modifier.size(100.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.account),
                        contentDescription = "Account",
                        tint = Color.Unspecified
                    )
                }

                IconButton(onClick = { /* TODO: Navigate to Settings */ },
                        modifier = Modifier.size(100.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "Settings",
                        tint = Color.Unspecified,
                    )
                }

            }
        }
    ) { innerPadding ->

        //inside the scaffold, this would be the content of the screen, start editing screen content from in here

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState) // Makes the content scrollable
        ) {
            // -- Photo Carousel Section --
            campsiteCarousel.displayCarousel(
                modifier = Modifier.fillMaxWidth(),
                photoCarousel = campsiteCarousel
            )

            // Content below the carousel gets side padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // -- Header Section --

                    // Rating
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically) {
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

                // -- Information Grid Section --
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Column
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically)
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
                            modifier = Modifier.weight(1f)
                        ) // Hardcoded price placeholder

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "🏕️ Campsite Rules",
                            fontSize = 16.sp,
                            color = Color.Blue,
                            modifier = Modifier.weight(1f)
                        )

                    }


                    // Right Column
                    Column {
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
                                modifier = Modifier.weight(1f)
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

                // -- Description Section --
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

                // -- Lottery / Waitlist Section --
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Lottery Status: Ended",//No lottery thing implemented yet
                        color = Color(0xFFA13F3F), // Dark red text
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { /* TODO: Implement Waitlist Logic */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF2C9C9) // Pinkish button color
                        ),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Click to join Waitlist!!",
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "50+ people currently waiting\n5 days left", //nothing about lottery or waitlist implemented yet
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp)) // Extra padding at the bottom
            }
        }

        }
    }
