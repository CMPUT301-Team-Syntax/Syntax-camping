package com.example.syntax_camping.screens.userScreens.userCampsiteList

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.R
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.lotteryRepository
import com.example.syntax_camping.screens.Rating

@Composable
fun CampsiteCard(campsite: Campsite, onClick: () -> Unit) {
    val lotteryId = campsite.lotteryId?.takeIf { it.isNotBlank() }
    var lottery by remember(lotteryId) { mutableStateOf<Lottery?>(null) }
    var lotteryLoaded by remember(lotteryId) { mutableStateOf(false) }
    var waitingCount by remember(lotteryId) { mutableIntStateOf(0) }

    LaunchedEffect(lotteryId) {
        if (lotteryId != null) {
            val repo = lotteryRepository()
            repo.getLottery(lotteryId) { fetched ->
                val validLottery = fetched?.takeIf { !it.status.isNullOrBlank() }
                lottery = validLottery
                lotteryLoaded = true

                if (validLottery?.status == "initialAccepting") {
                    repo.getEntries(lotteryId) { entries -> waitingCount = entries.size }
                } else if (validLottery?.status == "booking") {
                    repo.getWaitlist(lotteryId) { waitlist -> waitingCount = waitlist.size }
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
            .testTag("campsiteCard_${campsite.documentId}"),
        shape = RoundedCornerShape(33.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF2994A))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = campsite.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.location_on),
                        contentDescription = "location",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Black
                    )
                    Text(
                        text = campsite.location,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }

                val shownTags = campsite.tags.filter { it.isNotBlank() }
                if (shownTags.isNotEmpty()) {
                    Text(
                        text = shownTags.joinToString("  ") { "#$it" },
                        fontSize = 12.sp,
                        color = Color(0xFF3E6B68),
                        modifier = Modifier.testTag("campsiteTags_${campsite.documentId}")
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.occupancy),
                        contentDescription = "Capacity",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${campsite.maxOcc} people max",
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }

                val shownLottery = lottery
                when {
                    lotteryId == null -> {
                        Text(
                            text = "No lotteries available",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                    !lotteryLoaded -> {
                        Text(
                            text = "Loading lottery...",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                    shownLottery == null || shownLottery.status == "complete" -> {
                        Text(
                            text = "Lottery ended",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                    else -> {
                        val targetTime = if (shownLottery.status == "booking") {
                            shownLottery.endTimeBooking
                        } else {
                            shownLottery.endTimeLottery
                        }
                        val daysLeft = ((targetTime - System.currentTimeMillis()) / 86_400_000L).coerceAtLeast(0)

                        Text(
                            text = "$waitingCount people currently waiting",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "$daysLeft days left",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                Rating(
                    numRating = campsite.rating,
                    modifier = Modifier.testTag("Rating")
                )
            }
        }
    }
}