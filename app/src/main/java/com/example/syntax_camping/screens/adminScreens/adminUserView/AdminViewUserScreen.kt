package com.example.syntax_camping.screens.adminScreens.adminUserView

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.syntax_camping.R
import com.example.syntax_camping.dataClasses.NotificationLogScope
import com.example.syntax_camping.screens.adminScreens.openAdminNotificationLogs
import com.example.syntax_camping.screens.adminScreens.adminUserList.UserStatus
import com.example.syntax_camping.screens.tabs.AdminBottomBar
import com.example.syntax_camping.screens.tabs.TopBar

/** What this screen shows. Whoever builds the users repo fills this in. */
data class AdminUserDetails(
    val name: String,
    val email: String,
    val phone: String,
    val status: UserStatus?
)

/**
 * No data loading here. Pass the user in once the repo exists;
 * until then (user == null) it shows a placeholder.
 */
@Composable
fun AdminViewUserScreen(
    navController: NavController,
    userId: String,
    user: AdminUserDetails? = null
) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
            }

            if (user == null) {
                Text(
                    text = "No user data yet (id: $userId)",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.testTag("adminUserInfoNoData")
                )
            } else {
                InfoLine("Name", user.name.ifBlank { "Unnamed user" }, "adminUserInfoName")
                InfoLine("Email", user.email.ifBlank { "-" }, "adminUserInfoEmail")
                InfoLine("Phone", user.phone.ifBlank { "-" }, "adminUserInfoPhone")
                InfoLine("Status", user.status?.label ?: "-", "adminUserInfoStatus")
                TextButton(
                    onClick = { navController.openAdminNotificationLogs(NotificationLogScope.OWNER, userId) }
                ) { Text(stringResource(R.string.admin_view_notification_logs)) }
            }
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