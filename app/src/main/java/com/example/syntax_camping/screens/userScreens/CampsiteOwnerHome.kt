package com.example.syntax_camping.screens.userScreens

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.CampsiteOwner
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.CampsiteListScreenOwner
import com.example.syntax_camping.screens.CustomBottomBar
import com.example.syntax_camping.screens.TopBar

@Composable

fun CampsiteOwnerHome(navController: NavController, onEditCampsite: (Campsite?) -> Unit){

    val sampleCampsites = remember {
        CampsiteRepository.getCampsites().toList()
    }//this owner just owns all the campsites for testing
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
            TopBar(
                navController
            )
        },

        bottomBar = {
            CustomBottomBar(navController,
                "CampsiteOwnerHome")
        }
    ){innerPadding ->

        Column( modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.spacedBy(28.dp)
            ){

                IconButton(onClick = {onEditCampsite(null)},
                    modifier = Modifier.size(40 .dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.add),
                        contentDescription = "add campsite",
                        tint = Color.Unspecified
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (campsiteOwner.campsites.isNotEmpty()) {
                    CampsiteListScreenOwner(//owners version of list
                        campsiteList = campsiteOwner.campsites,
                        onItemClick = { clickedId ->
                            navController.navigate("campsiteDetails/$clickedId")
                        },
                        onUpdateClick = { clickedId ->
                            val clickedCamp = campsiteOwner.campsites.find { it.documentId == clickedId }
                            onEditCampsite(clickedCamp)
                        }
                    )
                }
            }
        }
    }

    // displaying the camp

}

