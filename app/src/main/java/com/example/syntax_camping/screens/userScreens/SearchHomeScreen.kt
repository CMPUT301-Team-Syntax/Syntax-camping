package com.example.syntax_camping.UserScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.CampsiteListScreen
import com.example.syntax_camping.screens.TopBar
import androidx.compose.runtime.LaunchedEffect
import com.example.syntax_camping.screens.CustomBottomBar

@Composable
fun SearchHomeScreen(navController: NavController){

    var  SearchInput by remember { mutableStateOf("")  }
    var FlitteredCampsiteList by remember { mutableStateOf(listOf<Campsite>()) }

    var realCampsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

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
                currentRoute = "Search",
                backgroundColor = Color(0xFF3E6B68)
            )
        }
    ) { innerPadding ->

        //inside the scaffold, this would be the content of the screen, start editing screen content from in here

        Column(
            modifier = Modifier

                .padding(innerPadding)
                .padding(9.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(){

                TextField(
                    value = SearchInput,
                    onValueChange = { newValue -> SearchInput = newValue },
                    label = { Text("Type in the location or Campsite Name") },
                    modifier = Modifier

                        .padding(bottom = 16.dp)
                )

                LaunchedEffect(Unit) {
                    CampsiteRepository.refreshCampsiteCache { success ->
                        if (success) {
                            // If Firebase succeeded, grab the newly downloaded list
                            realCampsites = CampsiteRepository.getCampsites().toList()
                        }
                    }
                }

                IconButton(onClick = {
                    FlitteredCampsiteList = realCampsites.filter { campsite ->
                        campsite.name.contains(SearchInput, ignoreCase = true) || campsite.location.contains(SearchInput, ignoreCase = true)
                    }



                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Search",
                        tint = Color.Unspecified
                    )
                }
            }


//
            Box(modifier = Modifier.weight(1f)) {

                if (realCampsites.isEmpty()) {
                    // Show this if the database is empty or failed to load
                    Text("No campsites found in database.")
                } else {
                    // Show the actual list of cards!
                    CampsiteListScreen(
                        campsiteList = realCampsites,
                        onItemClick = { clickedId ->
                            navController.navigate("campsiteDetails/$clickedId")
                        }
                    )
                }

            }
        }
    }
}