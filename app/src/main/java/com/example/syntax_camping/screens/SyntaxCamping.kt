package com.example.syntax_camping.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.syntax_camping.UserScreens.CampsiteDetailsScreen
import com.example.syntax_camping.UserScreens.ProfileScreen
import com.example.syntax_camping.UserScreens.ReservationsScreen
import com.example.syntax_camping.UserScreens.SearchHomeScreen
import com.example.syntax_camping.UserScreens.SettingsScreen
import com.example.syntax_camping.screens.userScreens.CampsiteSummaryScreen

@Composable
fun SyntaxCamping(
    modifier: Modifier = Modifier
){
    val navController = rememberNavController()
//PLEASE NOTE: CampsiteDetailsScreen IS THE CURRENT NON FUNCTIONAL TEMPLATE SCREEN FOR COPYING
    //THE OTHER SCREENS ARE TO BE IMPLEMENT AS THE SCREEN NAME SUGGESTS AND THE USER STORIES
//    androidx.navigation.NavHost(navController = navController, startDestination = "Search")
    NavHost(navController = navController, startDestination = "home", modifier = modifier) {
        composable("home") {
            CampsiteDetailsScreen(navController)
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

        composable("test") {
            CampsiteSummaryScreen(navController)
        }

    }

}