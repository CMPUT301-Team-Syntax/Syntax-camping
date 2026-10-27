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
import androidx.compose.runtime.Composable
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

@Composable
fun SearchHomeScreen(navController: NavController){
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

@Composable
fun CampsiteListScreen(campsitelist: List<Campsite>, onItemClick: (String) -> Unit){
    Scaffold { innerPadding ->
        LazyColumn(Modifier
            .fillMaxSize()
            .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ){
            items(campsitelist.size){index ->
                CampsiteRowTab(campsitelist[index]){
                    onItemClick(campsitelist[index].getDocumentId())
                }
            }
        }
    }
}

@Composable
fun CampsiteRowTab(campsite: Campsite, onClick:() -> Unit){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(33.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE2AFAF)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        )

    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically

        ){
            Box(
                contentAlignment = Alignment.CenterStart
            ){
                if (campsite.getPhotos().isNotEmpty()){
                    AsyncImage(
                        model = campsite.getPhotos()[0],
                        contentDescription = " photo",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(8.dp)
                    )
                }
                else{
                    Image(
                        painter = painterResource(id = R.drawable.home),
                        contentDescription = "No Image",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(8.dp)
                    )
                }

            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                Text(
                    text = campsite.getName(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
                Text(text = campsite.getLocation()
                    , fontSize = 14.sp,
                    color = Color.Black,)

            }

        }



    }
}




@Preview(showBackground = true)
@Composable
fun SearchHomeScreenPreview() {
    val sampleCampsites = listOf(
        Campsite("1", "Pine Ridge Campground", "Banff, AB", "Beautiful mountain view site.", 4, mutableListOf()),
        Campsite("2", "Lakeside Haven", "Jasper, AB", "Quiet site right next to the lake.", 6, mutableListOf())
    )
    CampsiteListScreen(
        campsitelist = sampleCampsites,
        onItemClick = {}
    )
}