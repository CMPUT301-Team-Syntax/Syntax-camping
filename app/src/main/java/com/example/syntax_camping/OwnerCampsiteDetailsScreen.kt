package com.example.syntax_camping

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// Template for US 02.01.04.
// TODO: Add campsite details later.

@Composable
fun OwnerCampsiteDetailsScreen(navController: NavController) {
    val pink = Color(0xFFE2AFAF)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(pink),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OwnerDetailsIcon(
                    icon = R.drawable.back,
                    label = "Back"
                ) {
                    if (!navController.popBackStack()) {
                        navController.navigate("MyCampsites") {
                            launchSingleTop = true
                        }
                    }
                }

                OwnerDetailsIcon(
                    icon = R.drawable.notification,
                    label = "Notifications"
                ) {
                    navController.navigate("Notifications") {
                        launchSingleTop = true
                    }
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(pink)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        navController.navigate("MyCampsites") {
                            popUpTo("MyCampsites") {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                ) {
                    Text("My Campsites", color = Color.Black)
                }

                OwnerDetailsIcon(
                    icon = R.drawable.reservations,
                    label = "Reservations"
                ) {
                    navController.navigate("Reservations") {
                        launchSingleTop = true
                    }
                }

                OwnerDetailsIcon(
                    icon = R.drawable.account,
                    label = "Profile"
                ) {
                    navController.navigate("Profile") {
                        launchSingleTop = true
                    }
                }

                OwnerDetailsIcon(
                    icon = R.drawable.ic_settings,
                    label = "Settings"
                ) {
                    navController.navigate("Settings") {
                        launchSingleTop = true
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Owner Campsite Details")
        }
    }
}

@Composable
private fun OwnerDetailsIcon(
    icon: Int,
    label: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(icon),
            contentDescription = label,
            tint = Color.Unspecified
        )
    }
}