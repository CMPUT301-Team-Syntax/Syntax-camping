package com.example.syntax_camping.screens.adminScreens.adminCampsiteList

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.syntax_camping.screens.CampsiteThumbnail
import com.example.syntax_camping.screens.Rating

/**
 * Admin version of the campsite card. Looks like the owner card, but the three-dot menu
 * always shows the admin actions. Tapping the card opens the campsite as an admin.
 */
@Composable
fun CampsiteCardAdmin(
    campsite: Campsite,
    onClick: () -> Unit,
    onViewLogsClick: (() -> Unit)? = null,
    onViewOwnerProfileClick: (() -> Unit)? = null,
    onViewNotificationsClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    val lotteryId = campsite.lotteryId?.takeIf { it.isNotBlank() }
    var lottery by remember(lotteryId) { mutableStateOf<Lottery?>(null) }
    var lotteryLoaded by remember(lotteryId) { mutableStateOf(false) }

    LaunchedEffect(lotteryId) {
        if (lotteryId != null) {
            lotteryRepository().getLottery(lotteryId) { fetched ->
                lottery = fetched?.takeIf { !it.status.isNullOrBlank() }
                lotteryLoaded = true
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
            .testTag("campsiteCardAdmin_${campsite.documentId}"),
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
            CampsiteThumbnail(campsite)

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
                        modifier = Modifier.testTag("campsiteTagsAdmin_${campsite.documentId}")
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
                val lotteryText = when {
                    lotteryId == null -> "No lottery available"
                    !lotteryLoaded -> "Loading lottery..."
                    shownLottery == null -> "Lottery not found"
                    shownLottery.status == "initialAccepting" -> "Lottery open · ${daysUntil(shownLottery.endTimeLottery)} days until the draw"
                    shownLottery.status == "booking" -> "Waitlist open · ${daysUntil(shownLottery.endTimeBooking)} days left"
                    shownLottery.status == "complete" -> "Lottery ended"
                    else -> "Lottery status unknown"
                }
                Text(
                    text = lotteryText,
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )

                Rating(
                    numRating = campsite.rating,
                    modifier = Modifier.testTag("Rating")
                )
            }

            Box {
                var menuExpanded by remember { mutableStateOf(false) }

                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("adminCampsiteMenu_${campsite.documentId}")
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.more_vertical),
                        contentDescription = "Options Menu",
                        tint = Color.Unspecified
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("View logs") },
                        modifier = Modifier.testTag("adminViewLogsItem"),
                        onClick = {
                            menuExpanded = false
                            onViewLogsClick?.invoke()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("View owner profile") },
                        modifier = Modifier.testTag("adminViewOwnerProfileItem"),
                        onClick = {
                            menuExpanded = false
                            onViewOwnerProfileClick?.invoke()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("View notifications") },
                        modifier = Modifier.testTag("adminViewNotificationsItem"),
                        onClick = {
                            menuExpanded = false
                            onViewNotificationsClick?.invoke()
                        }
                    )
                    if (onDeleteClick != null) {  //Incase some campsite doesn't delete or sum
                        DropdownMenuItem(
                            text = {
                                Text("Delete Campsite", color = Color.Red)
                            },
                            modifier = Modifier.testTag("adminDeleteCampsiteItem"),
                            onClick = {
                                menuExpanded = false
                                onDeleteClick.invoke()
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun daysUntil(timeMillis: Long): Long =
    ((timeMillis - System.currentTimeMillis()) / 86_400_000L).coerceAtLeast(0)
