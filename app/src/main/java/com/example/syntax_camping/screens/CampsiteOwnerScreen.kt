package com.example.syntax_camping.screens

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
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.screens.ownerScreens.CampsiteDetailsLotteryScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteEditingScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteMessagesScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteOwnerHomeScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteProfileScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteSettingsScreen
import androidx.compose.runtime.LaunchedEffect


@Composable

fun CampsiteOwnerScreen(modifier: Modifier = Modifier) {

    val navController = rememberNavController()
    var isLoading by remember { mutableStateOf(true) }
    var rp = CampsiteRepository()
    var documentId = "Test12"
    var selCampsite by remember { mutableStateOf<Campsite?>(null) }


    NavHost(navController = navController, startDestination = "campsiteOwner"){
        composable("campsiteOwner"){
            CampsiteOwnerHomeScreen(navController)
        }
        composable("campsiteLottery"){
            CampsiteDetailsLotteryScreen(navController)
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
        composable( "UpdateCamp"){

           //val selCampsite = Campsite("Test12", "Name", "Lop", "This is a description", 10, mutableListOf<String>())
            LaunchedEffect(documentId) {
                rp.getCampsite(documentId) { campsite ->
                    selCampsite = campsite
                }
            }
            selCampsite = null

           CampsiteEditingScreen(
               navController,
               selCampsite)

             }


    }
}


