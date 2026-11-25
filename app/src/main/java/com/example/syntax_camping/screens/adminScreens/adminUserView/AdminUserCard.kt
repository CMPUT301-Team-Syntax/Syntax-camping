package com.example.syntax_camping.screens.adminScreens.adminUserList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** What the admin sees on a user card. */
enum class UserStatus(val label: String) {
    OWNER("owner"),
    USER("user"),
    USER_AND_OWNER("user and owner")
}

/**
 * hasCampsites: owns at least one campsite.
 * inLotteryActivity: in any lottery, reservation or waitlist.
 * Returns null when neither is true so the card shows a blank status.
 */
fun userStatusOf(hasCampsites: Boolean, inLotteryActivity: Boolean): UserStatus? = when {
    hasCampsites && inLotteryActivity -> UserStatus.USER_AND_OWNER
    hasCampsites -> UserStatus.OWNER
    inLotteryActivity -> UserStatus.USER
    else -> null
}

/** Plain data for one row, so the card doesn't depend on how users are loaded. */
data class AdminUserItem(
    val id: String,
    val name: String,
    val status: UserStatus?
)

@Composable
fun UserInfoCard(user: AdminUserItem, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
            .testTag("userInfoCard_${user.id}"),
        shape = RoundedCornerShape(33.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = user.name.ifBlank { "Unnamed user" },
                fontSize = 12.sp,
                color = Color.Black,
                modifier = Modifier.testTag("userInfoName_${user.id}")
            )
            Text(
                text = user.status?.label ?: "",
                fontSize = 12.sp,
                color = Color(0xFF3E6B68),
                modifier = Modifier.testTag("userInfoStatus_${user.id}")
            )
        }
    }
}