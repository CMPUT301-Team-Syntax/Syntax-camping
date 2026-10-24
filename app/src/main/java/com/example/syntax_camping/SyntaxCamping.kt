package com.example.syntax_camping

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// App navigation.
// My Campsites is the temporary start page for owner UI testing.

@Composable
fun SyntaxCamping(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "MyCampsites",
        modifier = modifier
    ) {
        composable("home") {
            CampsiteDetailsScreen(navController)
        }

        composable("Search") {
            SearchHomeScreen(navController)
        }

        composable("Reservations") {
            ReservationsScreen(navController)
        }

        composable("Profile") {
            ProfileScreen(navController)
        }

        composable("Settings") {
            SettingsScreen(navController)
        }

        composable("MyCampsites") {
            MyCampsitesScreen(navController)
        }

        composable("OwnerCampsiteDetails") {
            OwnerCampsiteDetailsScreen(navController)
        }

        composable("Notifications") {
            // Temporary destination until the notification page is ready.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                TextButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Text("Back")
                }

                Text("Notifications")
            }
        }
    }
}