package com.example.syntax_camping.screens.adminScreens.adminUserView

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.R
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.NotificationLogScope
import com.example.syntax_camping.dataClasses.Reservation
import com.example.syntax_camping.dataClasses.Review
import com.example.syntax_camping.screens.adminScreens.adminCampsiteList.CampsiteCardAdmin
import com.example.syntax_camping.screens.adminScreens.adminUserList.UserStatus
import com.example.syntax_camping.screens.adminScreens.openAdminNotificationLogs
import com.example.syntax_camping.screens.tabs.AdminBottomBar
import com.example.syntax_camping.screens.tabs.TopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.syntax_camping.dataClasses.DeleteRepository
import com.example.syntax_camping.screens.userScreens.profileScreen.DeleteProfilePopUp
/**
 * What this screen shows. Whoever loads the data fills this in.
 * Everything after `status` has a default, so older calls still compile.
 */
data class AdminUserDetails(
    val name: String,
    val email: String,
    val phone: String,
    val status: UserStatus?,
    val profileImageUrl: String = "",
    val campsites: List<Campsite> = emptyList(),          // campsites this user owns
    val ownedLotteries: List<Lottery> = emptyList(),      // lotteries only this owner runs
    val sharedLotteries: List<Lottery> = emptyList(),     // lotteries shared with other owners
    val reservations: List<Reservation> = emptyList(),    // all of the user's reservations
    val reviews: List<Pair<Campsite, Review>> = emptyList() // reviews the user wrote, with the campsite
)

private val CardColor = Color(0xFFF4E8D5)

private fun formatDate(millis: Long): String =
    if (millis <= 0L) "" else SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))

/**
 * No data loading here. Pass the user in once it's loaded;
 * until then (user == null) it shows a placeholder.
 */
@Composable
fun AdminViewUserScreen(
    navController: NavController,
    userId: String,
    user: AdminUserDetails? = null
) {
    val deleteRepository = remember { DeleteRepository() }
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF5F5F5),
        contentColor = Color.Black,
        topBar = { TopBar(navController) },
        bottomBar = { AdminBottomBar(navController, currentRoute = "adminBrowseProfiles") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.testTag("adminUserInfoBack")
                ) { Text("Back") }
                Text(
                    text = "User info",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.weight(1f))

                //Delete button placement
                // Dropdown menu for administrative actions.
                if (user != null) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = Color.Red
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete User", color = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }

            if (user == null) {
                Text(
                    text = "No user data yet (id: $userId)",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier
                        .padding(16.dp)
                        .testTag("adminUserInfoNoData")
                )
            } else {
                UserContent(navController, userId, user)
            }

        }
    }

    //Delete confirmation screen
    if (showDeleteDialog) {
        DeleteProfilePopUp(
            onDismiss = { showDeleteDialog = false },
            isDeleting = isDeleting,
            onConfirmDelete = {
                if (!isDeleting) {
                    isDeleting = true

                    deleteRepository.removeUser(userId) { result ->

                        isDeleting = false

                        result.onSuccess {
                            showDeleteDialog = false

                            Toast.makeText(
                                context,
                                "User deleted successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            navController.popBackStack()

                        }.onFailure { error ->
                            Toast.makeText(
                                context,
                                error.message ?: "Failed to delete user",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun UserContent(navController: NavController, userId: String, user: AdminUserDetails) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp, end = 16.dp, bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // profile picture + name + status
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (user.profileImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = user.profileImageUrl,
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .testTag("adminUserInfoPicture")
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFCCCCCF))
                            .testTag("adminUserInfoNoPicture"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            user.name.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Column {
                    Text(
                        user.name.ifBlank { "Unnamed user" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("adminUserInfoName")
                    )
                    Text(
                        user.status?.label ?: "-",
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.testTag("adminUserInfoStatus")
                    )
                }
            }
        }

        // call to action for complaints
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("adminComplaints") }
                    .testTag("adminUserInfoComplaintsBanner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0DC))
            ) {
                Text(
                    "Tap to view complaints about this user",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFA13F3F)
                )
            }
        }

        // contact info
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoLine("Email", user.email.ifBlank { "-" }, "adminUserInfoEmail")
                InfoLine("Phone", user.phone.ifBlank { "-" }, "adminUserInfoPhone")
                TextButton(
                    onClick = { navController.openAdminNotificationLogs(NotificationLogScope.OWNER, userId) }
                ) { Text(stringResource(R.string.admin_view_notification_logs)) }
            }
        }

        // campsites owned
        item { SectionTitle("Campsites owned") }
        if (user.campsites.isEmpty()) {
            item { EmptyText("This user doesn't own any campsites.", "adminUserInfoNoCampsites") }
        } else {
            items(user.campsites, key = { "camp_${it.documentId}" }) { campsite ->
                CampsiteCardAdmin(
                    campsite,
                    onClick = { navController.navigate("ownerCampsiteDetails/${campsite.documentId}") }
                )
            }
        }

        // lotteries
        item { SectionTitle("Lotteries owned") }
        if (user.ownedLotteries.isEmpty()) {
            item { EmptyText("No lotteries owned.", "adminUserInfoNoLotteries") }
        } else {
            items(user.ownedLotteries, key = { "lot_${it.lotteryId}" }) { lottery ->
                LotteryCard(lottery) { navController.navigate("LotteryDetailsScreen/${lottery.lotteryId}") }
            }
        }

        item { SectionTitle("Shared lotteries (with other campsite owners)") }
        if (user.sharedLotteries.isEmpty()) {
            item { EmptyText("No shared lotteries.", "adminUserInfoNoSharedLotteries") }
        } else {
            items(user.sharedLotteries, key = { "shared_${it.lotteryId}" }) { lottery ->
                LotteryCard(lottery) { navController.navigate("LotteryDetailsScreen/${lottery.lotteryId}") }
            }
        }

        // reservations, one subsection per status
        item { SectionTitle("Reservations") }
        val groups = listOf(
            "Upcoming" to listOf("upcoming"),
            "Current" to listOf("current"),
            "Past" to listOf("past"),
            "Cancelled" to listOf("cancelled", "rejected")
        )
        for ((title, statuses) in groups) {
            val inGroup = user.reservations.filter { r ->
                statuses.any { r.status.trim().equals(it, ignoreCase = true) }
            }
            item { SubTitle("$title (${inGroup.size})") }
            if (inGroup.isEmpty()) {
                item { EmptyText("None.", "adminUserInfoNoRes_$title") }
            } else {
                items(inGroup, key = { "res_${title}_${it.reservationId}" }) { r ->
                    AdminReservationCard(r)
                }
            }
        }

        // reviews written by this user
        item { SectionTitle("Reviews written") }
        if (user.reviews.isEmpty()) {
            item { EmptyText("This user hasn't written any reviews.", "adminUserInfoNoReviews") }
        } else {
            items(user.reviews, key = { (c, r) -> "rev_${c.documentId}_${r.timestamp}" }) { (campsite, review) ->
                ReviewCard(campsite, review)
            }
        }

        // complaints (nothing to load yet)
        item { SectionTitle("Complaints by this user") }
        item { EmptyText("No complaints yet.", "adminUserInfoNoComplaintsBy") }
        item { SectionTitle("Complaints about this user") }
        item { EmptyText("No complaints yet.", "adminUserInfoNoComplaintsAbout") }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(top = 12.dp)
    )
}

@Composable
private fun SubTitle(text: String) {
    Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3E6B68))
}

@Composable
private fun EmptyText(text: String, tag: String) {
    Text(text, fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.testTag(tag))
}

@Composable
private fun LotteryCard(lottery: Lottery, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("adminLotteryCard_${lottery.lotteryId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(lottery.name.ifBlank { "Lottery" }, fontWeight = FontWeight.Bold)
            Text("Status: ${lottery.status ?: "-"}", fontSize = 13.sp)
            val names = lottery.campsites.joinToString { it.name.ifBlank { "Campsite" } }
            if (names.isNotBlank()) Text("Campsites: $names", fontSize = 13.sp)
        }
    }
}

@Composable
private fun AdminReservationCard(r: Reservation) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("adminReservationCard_${r.reservationId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(r.campsite.name.ifBlank { "Campsite" }, fontWeight = FontWeight.Bold)
            Text("Dates: ${r.datesReserved.firstOrNull() ?: ""} to ${r.datesReserved.lastOrNull() ?: ""}", fontSize = 13.sp)
            Text("Guests: ${r.numberGuests}", fontSize = 13.sp)
            if (r.bookingName.isNotBlank()) Text("Booked under: ${r.bookingName}", fontSize = 13.sp)
            if (r.cancelReason.isNotBlank()) Text("Cancel reason: ${r.cancelReason}", fontSize = 13.sp)
        }
    }
}

@Composable
private fun ReviewCard(campsite: Campsite, review: Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("adminReviewCard_${campsite.documentId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(campsite.name.ifBlank { "Campsite" }, fontWeight = FontWeight.Bold)
            Text("Rating: ${review.rating}", fontSize = 13.sp)
            if (review.reviewText.isNotBlank()) Text(review.reviewText, fontSize = 14.sp)
            val date = formatDate(review.timestamp)
            if (date.isNotBlank()) Text(date, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String, tag: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(
            value,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.testTag(tag)
        )
    }
}