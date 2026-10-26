package com.example.syntax_camping.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.syntax_camping.screens.ownerScreens.CampsiteDetailslotteryScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteMessagesScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteOwnerHomeScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteProfileScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteSettingsScreen


@Composable

fun campsiteOwnerScreen(modifier: Modifier = Modifier) {

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


