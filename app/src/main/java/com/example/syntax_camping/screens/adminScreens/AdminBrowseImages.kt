package com.example.syntax_camping.screens.adminScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.tabs.AdminBottomBar
import com.example.syntax_camping.screens.tabs.TopBar

@Composable
fun AdminBrowseImages(navController: NavController, ) {
    var searchImage by remember { mutableStateOf("") }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF5F5F5),
        contentColor = Color.Black,
        topBar = { TopBar(navController) },
        bottomBar = { AdminBottomBar(navController, currentRoute = "adminBrowseImages") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
        ) {
            // Header Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Browse Images",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Search Bar Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchImage,
                    onValueChange = { searchImage = it },
                    label = { Text("Type in the location or Campsite Name") },
                    shape = RoundedCornerShape(30.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,   // Removes line when typing
                        unfocusedIndicatorColor = Color.Transparent, // Removes line when idle
                        disabledIndicatorColor = Color.Transparent   // Removes line if disabled
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_image_field"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        // keeps any near-me search that was already applied
                    },
                    modifier = Modifier
                        .background(Color(0xFF3E6B68), shape = RoundedCornerShape(50))
                        .testTag("search_icon")
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Search",
                        tint = Color.Unspecified
                    )
                }
            }

            // Main Content Area (Placeholder / Grid)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Browse images",
                    color = Color.DarkGray,
                    fontSize = 16.sp,
                    modifier = Modifier.testTag("adminBrowseImagesPlaceholder")
                )
            }
        }
    }
}

@Composable
fun CardToDisplay(
    modifier: Modifier = Modifier,
    uploadedBy: String = "User",
    postedOn: String = "Today",
    onMenuClick: () -> Unit = {},
    image: Painter = painterResource(id = android.R.drawable.presence_invisible)
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = image,
                    contentDescription = "Uploaded image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(110.dp)
                        .background(Color.LightGray, shape = RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Uploaded by: $uploadedBy", color = Color.Black, fontSize = 13.sp)
                    Text("Posted on: $postedOn", color = Color.Gray, fontSize = 13.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Image details",
                        color = Color.DarkGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                IconButton(
                    onClick = onMenuClick
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_three_dots),
                        contentDescription = "More options",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CardToDisplayPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        CardToDisplay(
            uploadedBy = "John Doe",
            postedOn = "2025-03-30"
        )
    }
}