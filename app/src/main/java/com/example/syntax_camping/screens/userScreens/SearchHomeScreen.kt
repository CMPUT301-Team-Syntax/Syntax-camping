package com.example.syntax_camping.UserScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.CampsiteListScreen

@Composable
fun SearchHomeScreen(navController: NavController){

    var  SearchInput by remember { mutableStateOf("")  }
    // var FlitteredCampsiteList by remember { mutableStateListOf(List<Campsite>) }
    Scaffold(

        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Set the background color to white
        containerColor = Color.White,
        topBar = { // top bar with back button and notification icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
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

                IconButton(onClick = { /* TODO: Navigate to notifications */ },
                    modifier = Modifier.size(100.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.notification),
                        contentDescription = "notifications",
                        tint = Color.Unspecified
                    )
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
                IconButton(onClick = {
                    // FlitteredCampsiteList = campsiteList.filter { campsite ->
                    //     campsite.name().contains(SearchInput, ignoreCase = true) || campsite.location().contains(SearchInput, ignoreCase = true)
                    //
                    // }
                    // Box(modifier = Modifier.weight(1f)) {
                    //                CampsiteListScreen(
                    //                    campsiteList = FlitteredCampsiteList,
                    //                    onItemClick = {}
                    //                )
                    //            }


                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Search",
                        tint = Color.Unspecified
                    )
                }
            }

            val sampleCampsites = listOf(
                Campsite("1", "Pine Ridge Campground", "Banff, AB", "Beautiful mountain view site.", 4, mutableListOf()),
                Campsite("2", "Lakeside Haven", "Jasper, AB", "Quiet site right next to the lake.", 6, mutableListOf())
            )

            Box(modifier = Modifier.weight(1f)) {
                CampsiteListScreen(
                    campsiteList = sampleCampsites,
                    onItemClick = {}
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun SearchHomeScreenPreview() {

}