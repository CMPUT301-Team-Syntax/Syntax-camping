package com.example.syntax_camping.screens.adminScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.syntax_camping.screens.adminScreens.adminUserList.AdminUserItem
import com.example.syntax_camping.screens.adminScreens.adminUserView.UserListScreen
import com.example.syntax_camping.screens.tabs.AdminBottomBar
import com.example.syntax_camping.screens.tabs.TopBar

/**
 * Admin "browse profiles" page: a scrollable list of every user.
 * No data loading here on purpose. The users repo passes the list in.
 */
@Composable
fun AdminBrowseProfiles(
    navController: NavController,
    users: List<AdminUserItem> = emptyList(),
    loading: Boolean = false
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
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.testTag("adminBrowseProfilesBack")
                ) { Text("Back") }
                Text(
                    text = "Profiles",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.testTag("adminBrowseProfilesTitle")
                )
            }

            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (users.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No profiles to show",
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.testTag("adminBrowseProfilesEmpty")
                    )
                }
            } else {
                UserListScreen(users = users) { userId ->
                    navController.navigate("adminUserInfo/$userId")
                }
            }
        }
    }
}