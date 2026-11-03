package com.example.syntax_camping.screens.ownerScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.CampsiteOwner
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.CampsiteListScreen

@Composable
fun CampsiteOwnerHomeScreen (navController: NavController){
    // dummy variable for now
//    val name by remember { mutableStateOf("") }
//    val location by remember { mutableStateOf("") }
//    val userId by remember { mutableStateOf("") }
//    val campsiteOwner = CampsiteOwner(name, location, userId)


    // dummy data for now
    val sampleCampsites = remember {
        listOf(
            Campsite("1", "Pine Ridge Campground", "Banff, AB", "Beautiful mountain view site.", 4, mutableListOf("Mountain", "Hiking"), 4.5),
            Campsite("2", "Lakeside Haven", "Jasper, AB", "Quiet site right next to the lake.", 6, mutableListOf("Lake", "Fishing"), 4.8)
        )
    }
    val campsiteOwner = remember {
        CampsiteOwner("owner1", "John Doe", "Banff, AB").apply {
            campsites.addAll(sampleCampsites)
        }
    }
    Scaffold(

        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Set the background color to white
        containerColor = Color.White,
        topBar = { // top bar with back button and notification icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFE2AFAF)), // sets the background color to pink lkie the figma mockups
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                //icon button for back button, template for adding icons

                IconButton(

                    onClick = { /* TODO: Navigate to notifications */ },
                    modifier = Modifier.size(100.dp) // the size is defaulted to 44.dp which is pretty small so you can change this if needed for other icons

                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "back button",
                        tint = Color.Unspecified  //without it the icon would be black
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ){
                    IconButton(onClick = {navController.navigate( "UpdateCamp")},
                        modifier = Modifier.size(30 .dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.add),
                            contentDescription = "notifications",
                            tint = Color.Unspecified
                        )
                    }
                    IconButton(onClick = { /* TODO: Navigate to notifications */ },
                        modifier = Modifier.size(40.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.notification),
                            contentDescription = "notifications",
                            tint = Color.Unspecified
                        )
                    }
                }

            }
        },

        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFFE2AFAF))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {


                IconButton(onClick = { navController.navigate("home") },
                    modifier = Modifier.size(80.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.home),
                        contentDescription = "Search",
                        tint = Color.Unspecified
                    )
                }

                IconButton(onClick = { navController.navigate("lottery") },
                    modifier = Modifier.size(80.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.lottery),
                        contentDescription = "Search",
                        tint = Color.Unspecified
                    )
                }

                IconButton(onClick = { navController.navigate("messenger") },
                    modifier = Modifier.size(80.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.messenger),
                        contentDescription = "Reservations",
                        tint = Color.Unspecified
                    )
                }

                IconButton(onClick = { navController.navigate("Profile") },
                    modifier = Modifier.size(80.dp)
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.account),
                        contentDescription = "Account",
                        tint = Color.Unspecified
                    )
                }

                IconButton(onClick = { navController.navigate("Settings") },
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "Settings",
                        tint = Color.Unspecified,
                    )
                }

            }
        }
    ){ innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (campsiteOwner.campsites.isNotEmpty()  ) {
                    CampsiteListScreen(campsiteOwner.campsites, onItemClick = {})
                }


            }

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // displaying the camp

}







