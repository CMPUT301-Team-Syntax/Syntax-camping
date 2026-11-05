package com.example.syntax_camping.UserScreens

import android.R.attr.subtitle
import android.R.attr.title
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.syntax_camping.R
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.dataClasses.User
import com.example.syntax_camping.screens.CustomBottomBar
import com.example.syntax_camping.screens.Rating
import com.example.syntax_camping.screens.TopBar

@Composable
fun ProfileScreen(navController: NavController, userViewModel: UserViewModel) {

    Scaffold(

        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Set the background color to white
        containerColor = Color.White,
        topBar = { // top bar with back button and notification icon
            TopBar(navController)
        },

        bottomBar = {
            CustomBottomBar(
                navController = navController,
                currentRoute = "Profile",
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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    ,
                shape = RoundedCornerShape(33.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                )

                {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF2994A)) // Orange placeholder
                    )

                    Spacer(modifier = Modifier.width(16.dp))


                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    )
                    {

                        Text(
                            text = " Name ",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Text(
                            text = " number",
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Text(
                            text = " gmail",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )

                        Text(
                            text = "number",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )


                    }

                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            // this just display them and make them clickable

            ListItem(
                headlineContent = { Text(text = "Edit Personal information") },
                modifier = Modifier.clickable {
                    navController.navigate("UpdateProfile")
                }
            )
            ListItem(
                headlineContent = { Text(text = "Security and Sign in") },
                modifier = Modifier.clickable { }
            )
            ListItem(
                headlineContent = { Text(text = "Change Preferences") },
                modifier = Modifier.clickable { }
            )
            ListItem(
                headlineContent = { Text(text = "View Reservation history") },
                modifier = Modifier.clickable { }
            )
            ListItem(
                headlineContent = { Text(text = "View Search history") },
                modifier = Modifier.clickable { }
            )
            ListItem(
                headlineContent = { Text(text = "Delete Account") },
                modifier = Modifier.clickable { }
            )
            ListItem(
                headlineContent = { Text(text = "Contact Customer Support") },
                modifier = Modifier.clickable { }
            )



            }
        }
    }
