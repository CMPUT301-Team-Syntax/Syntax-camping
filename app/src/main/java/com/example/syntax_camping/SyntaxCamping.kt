package com.example.syntax_camping

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun SyntaxCamping(
    modifier: Modifier = Modifier
){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "Search"){
        composable("home") { CampsiteDetailsScreen(navController)}
        composable("Settings") { SettingsScreen(navController)}
        composable("Search") { SearchHomeScreen(navController)}
        composable("Reservations") { ReservationsScreen(navController)}
        composable("Profile") { ProfileScreen(navController)}

    }

}