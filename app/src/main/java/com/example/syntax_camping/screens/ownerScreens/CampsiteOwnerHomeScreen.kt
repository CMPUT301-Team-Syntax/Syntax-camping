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
import com.example.syntax_camping.dataClasses.OwnerData
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.CustomOwnerBottomBar
import com.example.syntax_camping.screens.OwnerTopBar
import com.example.syntax_camping.screens.CampsiteListScreen


@Composable
fun CampsiteOwnerHomeScreen (navController: NavController){
    // dummy variable for now
//    val name by remember { mutableStateOf("") }
//    val location by remember { mutableStateOf("") }
//    val userId by remember { mutableStateOf("") }


    // dummy data for now
    val sampleCampsites = remember {
        listOf(
            Campsite("1", "Pine Ridge Campground", "Banff, AB", "Beautiful mountain view site.", 4, mutableListOf("Mountain", "Hiking"), 4.5),
            Campsite("2", "Lakeside Haven", "Jasper, AB", "Quiet site right next to the lake.", 6, mutableListOf("Lake", "Fishing"), 4.8)
        )
    }
    val campsiteOwner = remember {
        OwnerData(location = "Banff, AB", campsites = sampleCampsites.toMutableList())
    }
    Scaffold(

        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Set the background color to white
        containerColor = Color.White,
        topBar = { // top bar with back button and notification icon
            OwnerTopBar(
                navController
            )
        },

        bottomBar = {
            CustomOwnerBottomBar(navController,
                "home")
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

@Composable
fun CampRow(campsiteName: String, campsiteLocation: String, campsiteLotteryStatus: String){
    Row(){
        Text(text = campsiteName)
    }
    Row(){
        Text(text = campsiteLocation,)
    }
    Row(){
        Text(text = campsiteLotteryStatus,)
    }

}





