package com.example.syntax_camping.UserScreens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.net.Uri
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.TextButton
import com.example.syntax_camping.R
import coil3.compose.AsyncImage
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.TopBar
import com.example.syntax_camping.screens.userScreens.profileScreen.DeleteProfilePopUp

@Composable
fun ProfileScreen(navController: NavController, userViewModel: UserViewModel) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                imageUri = uri
            }
        }
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        containerColor = Color.White,
        topBar = {
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(33.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF2994A))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ){
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Selected image",
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.account),
                                contentDescription = "Default profile icon",
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    SmallFloatingActionButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_plus),
                            contentDescription = "Change photo"
                        )
                    }

                    if (imageUri != null) {
                        TextButton(
                            onClick ={}
                        ){{}}
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = " Name: " + userViewModel.currentUser?.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Text(
                            text = " User ID: " + userViewModel.deviceId,
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Text(
                            text = " Gmail " + userViewModel.currentUser?.email,
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )

                        Text(
                            text = " Phone Number: " + userViewModel.currentUser?.phoneNumber,
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                modifier = Modifier.clickable {navController.navigate("PastReservations") }
            )
            ListItem(
                headlineContent = { Text(text = "View Search history") },
                modifier = Modifier.clickable {
                    navController.navigate("History")
                }
            )
            ListItem(
                headlineContent = { Text(text = "Delete Account") },
                modifier = Modifier.clickable {
                    showDeleteDialog = true
                }
            )
            ListItem(
                headlineContent = { Text(text = "Contact Customer Support") },
                modifier = Modifier.clickable { }
            )
        }

        if (showDeleteDialog) {
            DeleteProfilePopUp(
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}
