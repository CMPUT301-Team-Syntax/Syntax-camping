package com.example.syntax_camping.screens.adminScreens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.dataClasses.NotificationLogScope
import com.example.syntax_camping.screens.adminScreens.adminCampsiteList.CampsiteListScreenAdmin
import com.example.syntax_camping.screens.tabs.AdminBottomBar
import com.example.syntax_camping.screens.tabs.TopBar

@Composable
fun AdminBrowseCampsites(navController: NavController) {
    // start from whatever is already cached, then refresh through the repo
    var campsites by remember { mutableStateOf<List<Campsite>>(CampsiteRepository.getCampsites().toList()) }

    LaunchedEffect(Unit) {
        CampsiteRepository.refreshCampsiteCache { success ->
            if (success) campsites = CampsiteRepository.getCampsites().toList()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF5F5F5),
        contentColor = Color.Black,
        topBar = { TopBar(navController) },
        bottomBar = { AdminBottomBar(navController, currentRoute = "adminBrowseCampsites") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Browse ALL Campsites",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.testTag("adminBrowseCampsitesTitle")
                )
            }

            if (campsites.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No campsites to show",
                        modifier = Modifier.testTag("adminBrowseCampsitesEmpty")
                    )
                }
            } else {
                CampsiteListScreenAdmin(
                    campsiteList = campsites,
                    // for now this opens the owner-style details page; swap in the
                    // admin details route here once that page exists
                    onItemClick = { id -> navController.navigate("ownerCampsiteDetails/$id") },
                    onViewNotificationsClick = { id ->
                        navController.openAdminNotificationLogs(NotificationLogScope.CAMPSITE, id)
                    }
                )
            }
        }
    }
}