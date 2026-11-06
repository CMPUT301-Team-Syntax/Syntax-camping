package com.example.syntax_camping.screens

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.UserScreens.CampsiteDetailsScreen
import com.example.syntax_camping.UserScreens.ProfileScreen
import com.example.syntax_camping.UserScreens.ReservationsScreen
import com.example.syntax_camping.UserScreens.SearchHomeScreen
import com.example.syntax_camping.UserScreens.SettingsScreen
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.screens.ownerScreens.CampsiteDetailsLotteryScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteEditingScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteMessagesScreen
import com.example.syntax_camping.screens.userScreens.CampsiteOwnerHome
import com.example.syntax_camping.screens.userScreens.UpdateProfileScreen
import com.example.syntax_camping.screens.ownerScreens.LotteryEditScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteOwnerHome
import com.example.syntax_camping.dataClasses.Lottery

@Composable
fun SyntaxCamping(
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel
){
    val navController = rememberNavController()
    var selectedCampsite by remember { mutableStateOf<Campsite?>(null) }
    //i changed the name from of sel to selected for readability

    var isLoading by remember { mutableStateOf(true) }
    // var rp = CampsiteRepository()
    var documentId = "Test12"
    var selCampsite by remember { mutableStateOf<Campsite?>(null) }

    var selectedLottery by remember {mutableStateOf<Lottery?>(null) }

    NavHost(navController = navController, startDestination = "Search", modifier = modifier) {
        Log.d("UserDebug", "Current User: ${userViewModel.deviceId}")
        composable("campsiteDetails/{campsiteId}") { backStackEntry ->
            // Extract the ID from the navigation route
            val campsiteId = backStackEntry.arguments?.getString("campsiteId")
            CampsiteDetailsScreen(navController, campsiteId, userViewModel)
        }

        composable("ownerCampsiteDetails/{campsiteId}") { backStackEntry ->
            val campsiteId = backStackEntry.arguments?.getString("campsiteId")
            CampsiteDetailsScreen(
                navController = navController,
                campsiteID = campsiteId,
                userViewModel = userViewModel,
                update = true,
                onEditCampsite = { campsiteToEdit ->
                    selectedCampsite = campsiteToEdit
                    navController.navigate("UpdateCamp")
                }
            )
        }

        composable("Settings") {
            SettingsScreen(navController, userViewModel)
        }
        composable("Search") {
            SearchHomeScreen(navController, userViewModel)
        }
        composable("Reservations") {
            ReservationsScreen(navController, userViewModel)
        }
        composable("Profile") {
            ProfileScreen(navController, userViewModel)
        }
        composable("home") {
            CampsiteSummaryScreen(navController, userViewModel)
        }
        composable("CampsiteOwnerHome"){
            CampsiteOwnerHome(
                navController,
                userViewModel,
                onEditCampsite = { campsiteToEdit ->
                    selectedCampsite = campsiteToEdit
                    navController.navigate("UpdateCamp")
                },
                onEditLottery = { lotteryToEdit ->
                    selectedLottery = lotteryToEdit
                    navController.navigate("CreateLottery")
                }
            )
        }
        composable("campsiteLottery"){
            CampsiteDetailsLotteryScreen(navController, userViewModel)
        }
        composable("campsiteMessages"){
            CampsiteMessagesScreen(navController, userViewModel)
        }
        composable("CreateLottery"){
            val myOwnedCampsites = CampsiteRepository.getCampsites().toList()
         LotteryEditScreen(
             navController,
             userViewModel,
             myOwnedCampsites,
             selectedLottery
             )
        }

        composable ("LotteryDetailsScreen"){backStackEntry ->
            val lotteryId = backStackEntry.arguments?.getString("lotteryId")
            // Call your future Lottery Info View here and pass the lotteryId!
        }

        composable( "UpdateCamp") {
            CampsiteEditingScreen(
                navController,
                userViewModel,
                selectedCampsite)

        }
        composable("UpdateProfile"){
            UpdateProfileScreen(navController, userViewModel, modifier = Modifier)
        }



    }

}