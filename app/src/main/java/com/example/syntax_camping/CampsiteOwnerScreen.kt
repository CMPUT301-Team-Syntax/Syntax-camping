package com.example.syntax_camping

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


@Composable

fun campsiteOwnerScreen() {

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "campsiteOwner"){
        composable("campsiteOwner"){
            CampsiteOwnerHomeScreen(navController)
        }
        composable("campsitelottery"){
            CampsiteDetailslotteryScreen(navController)
        }
        composable("campsiteMessages"){
            CampsiteMessagesScreen(navController)
        }
        composable("campsiteProfile"){
            CampsiteProfileScreen(navController)
        }
        composable("campsiteSettings") {
            CampsiteSettingsScreen(navController)
        }
    }
}


