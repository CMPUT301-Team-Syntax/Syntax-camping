package com.example.syntax_camping.UserScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.syntax_camping.R
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.screens.CustomBottomBar
import com.example.syntax_camping.screens.TopBar

@Composable
fun SettingsScreen(navController: NavController, userViewModel: UserViewModel){
    Scaffold(

        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Set the background color to white
        containerColor = Color.White,
        topBar = {
            TopBar(navController)
        },

        bottomBar = {
            CustomBottomBar(
                navController = navController,
                currentRoute = "Settings",
                backgroundColor = Color(0xFF3E6B68)
            )
        }
    ) { innerPadding ->

        //inside the scaffold, this would be the content of the screen, start editing screen content from in here

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}