package com.example.syntax_camping

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
@Composable

fun campsiteOwnerScreen(modifier: Modifier = Modifier) {

    val navController = rememberNavController()
    var selectedCampsite by remember {
        mutableStateOf<Campsite?>(null)
    }

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
        composable( "UpdateCamp"){ CampsiteEditingScreen(navController,
            campsite = selectedCampsite)}
    }
}


