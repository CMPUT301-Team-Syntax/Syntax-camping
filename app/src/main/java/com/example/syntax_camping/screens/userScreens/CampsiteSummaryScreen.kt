package com.example.syntax_camping.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.CampsiteCard

@Composable
fun CampsiteSummaryScreen(navController: NavController){


    Scaffold(

        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4E8D5)), // Set the background color to white
        containerColor = Color(0xFFF4E8D5),
        topBar = {
            TopBar()
        },

        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFF3E6B68))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = { navController.navigate("Search") },
                    modifier = Modifier.size(100.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.search_tapped),
                        contentDescription = "Search",
                        tint = Color.Unspecified
                    )
                }

                IconButton(onClick = { navController.navigate("Reservations") },
                    modifier = Modifier.size(100.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.reservations),
                        contentDescription = "Reservations",
                        tint = Color.Unspecified
                    )
                }

                IconButton(onClick = { navController.navigate("Profile") },
                    modifier = Modifier.size(100.dp)
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.account),
                        contentDescription = "Account",
                        tint = Color.Unspecified
                    )
                }

                IconButton(onClick = { navController.navigate("Settings") },
                    modifier = Modifier.size(100.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "Settings",
                        tint = Color.Unspecified,
                    )
                }

            }
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
            //my brain is actually on fire how the hell do i test thissevijawifnalknfAJNVDKJKCkomJVAJNOJN GJNVPINU.

            val mockCampsites = remember {
                listOf(
                    Campsite(documentId = "1", name = "Dolor Campsite", location = "Edmonton", description = "Nice place", maxOcc = 9, tags = mutableListOf(), 4.0),
                    Campsite(documentId = "2", name = "Lorem Campsite", location = "Calgary", description = "Cool place", maxOcc = 4, tags = mutableListOf(),3.2),
                    Campsite(documentId = "3", name = "Ipsum Campsite", location = "Banff", description = "Cold place", maxOcc = 12, tags = mutableListOf(),4.4)
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(mockCampsites) { index, camp ->
                    CampsiteCard(camp, onClick = {})
                    if (index < mockCampsites.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }

        }
    }
}