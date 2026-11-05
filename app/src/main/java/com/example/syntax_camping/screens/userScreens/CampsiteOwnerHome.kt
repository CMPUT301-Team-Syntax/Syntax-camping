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
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.CampsiteListScreenOwner
import com.example.syntax_camping.screens.CustomBottomBar
import com.example.syntax_camping.screens.TopBar
import com.example.syntax_camping.dataClasses.User
import com.example.syntax_camping.dataClasses.UserType
import com.example.syntax_camping.dataClasses.OwnerData
import com.example.syntax_camping.dataClasses.UserProfile
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Composable

fun CampsiteOwnerHome(navController: NavController, onEditCampsite: (Campsite?) -> Unit){

    var ownerProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    //this is to be a commented as an ai prompt request, about testing this creation when its a data class

    //  Trigger a database refresh every time this screen appears
    LaunchedEffect(Unit) {
        isLoading = true
        CampsiteRepository.refreshCampsiteCache { success ->
            if (success) {
                val updatedCampsites = CampsiteRepository.getCampsites().toList()

                val baseUser = User(userId = "owner1", name = "John Doe", userType = UserType.Owner)
                val ownerData = OwnerData(location = "Banff, AB", campsites = updatedCampsites.toMutableList())

                ownerProfile = User(user = baseUser, ownerData = ownerData)
            }
            isLoading = false
        }
    }

    // Extract the list safely so we can use it in the UI below
    val ownerCampsites = ownerProfile?.ownerData?.campsites ?: emptyList()
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
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))}

                    else if (ownerCampsites.isNotEmpty()) {
                    CampsiteListScreenOwner(//owners version of list
                        campsiteList = ownerCampsites,
                        onItemClick = { clickedId ->
                            navController.navigate("ownerCampsiteDetails/$clickedId")
                        },
                        onUpdateClick = { clickedId ->
                            val clickedCamp = ownerCampsites.find { it.documentId == clickedId }
                            onEditCampsite(clickedCamp)})
                    }
                else {
                    Text("No campsites found.", modifier = Modifier.align(Alignment.Center))
                }
                }
            }
        }
    }
