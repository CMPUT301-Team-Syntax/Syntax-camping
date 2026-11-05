package com.example.syntax_camping.screens

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.UserScreens.CampsiteDetailsScreen
import com.example.syntax_camping.UserScreens.ProfileScreen
import com.example.syntax_camping.UserScreens.ReservationsScreen
import com.example.syntax_camping.UserScreens.SearchHomeScreen
import com.example.syntax_camping.UserScreens.SettingsScreen
import com.example.syntax_camping.screens.CampsiteSummaryScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteDetailsLotteryScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteEditingScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteMessagesScreen
import com.example.syntax_camping.screens.ownerScreens.MyCampsitesScreen
import com.example.syntax_camping.screens.ownerScreens.OwnerCampsiteDetailsScreen
import com.example.syntax_camping.screens.ownerScreens.loadOwnerCampsitesFromRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException

@Composable
fun SyntaxCamping(
    modifier: Modifier = Modifier,
    startDestination: String = "Search",
    loadOwnerCampsites: suspend () -> List<Campsite> = ::loadOwnerCampsitesFromRepository
){
    val navController = rememberNavController()
    var selectedCampsite by remember { mutableStateOf<Campsite?>(null) }
    //i changed the name from of sel to selected for readability

    var ownerCampsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }
    var ownerIsLoading by remember { mutableStateOf(true) }
    var ownerError by remember { mutableStateOf<String?>(null) }
    var ownerRefreshRequest by remember { mutableIntStateOf(0) }
    val entry by navController.currentBackStackEntryAsState()

    LaunchedEffect(entry, ownerRefreshRequest, loadOwnerCampsites) {
        val route = entry?.destination?.route
        if (route != "CampsiteOwnerHome" && route != "ownerCampsiteDetails/{campsiteId}") {
            return@LaunchedEffect
        }
        ownerIsLoading = true
        ownerError = null
        try {
            ownerCampsites = loadOwnerCampsites().toList()
        } catch (_: TimeoutCancellationException) {
            ownerError = "Loading campsites took too long. Please try again."
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            ownerError = error.message ?: "Could not load campsites. Please try again."
        } finally {
            ownerIsLoading = false
        }
    }

    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {

        composable("campsiteDetails/{campsiteId}") { backStackEntry ->
            // Extract the ID from the navigation route
            val campsiteId = backStackEntry.arguments?.getString("campsiteId")
            CampsiteDetailsScreen(navController, campsiteId)
        }

        composable("ownerCampsiteDetails/{campsiteId}") { backStackEntry ->
            val campsiteId = backStackEntry.arguments?.getString("campsiteId")
            OwnerCampsiteDetailsScreen(
                navController = navController,
                campsite = ownerCampsites.find { it.documentId == campsiteId },
                isLoading = ownerIsLoading,
                errorMessage = ownerError,
                onRetry = { ownerRefreshRequest++ },
                onEditCampsite = { campsiteToEdit ->
                    selectedCampsite = campsiteToEdit
                    navController.navigate("UpdateCamp")
                }
            )
        }

        composable("Settings") {
            SettingsScreen(navController)
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
        composable("home") {
            CampsiteSummaryScreen(navController)
        }
        composable("CampsiteOwnerHome"){
            MyCampsitesScreen(
                navController = navController,
                campsites = ownerCampsites,
                isLoading = ownerIsLoading,
                errorMessage = ownerError,
                onRetry = { ownerRefreshRequest++ },
                onCampsiteClick = { campsite ->
                    navController.navigate("ownerCampsiteDetails/${Uri.encode(campsite.documentId)}")
                },
                onAddCampsite = {
                    selectedCampsite = null
                    navController.navigate("UpdateCamp")
                }
            )
        }
        composable("campsiteLottery"){
            CampsiteDetailsLotteryScreen(navController)
        }
        composable("campsiteMessages"){
            CampsiteMessagesScreen(navController)
        }


        composable( "UpdateCamp") {
            CampsiteEditingScreen(
                navController,
                selectedCampsite)

        }


    }

}