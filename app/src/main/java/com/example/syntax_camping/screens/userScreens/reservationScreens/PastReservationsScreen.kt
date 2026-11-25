package com.example.syntax_camping.screens.userScreens.reservationScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.TopBar

@Composable
fun PastReservationsScreen(
    navController: NavController,
    userViewModel: UserViewModel
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("pastReservationsScreen"),
        containerColor = Color.White,
        topBar = {
            TopBar(navController)
        },
        bottomBar = {
            CustomBottomBar(
                navController = navController,
                currentRoute = "Reservations",
                backgroundColor = Color(0xFF3E6B68)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Past Reservations",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "No past reservations found.",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}