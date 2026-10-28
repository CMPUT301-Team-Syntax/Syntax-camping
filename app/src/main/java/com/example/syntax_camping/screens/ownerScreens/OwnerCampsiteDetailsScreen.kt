package com.example.syntax_camping.screens.ownerScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.R

// US 02.01.04: displays the selected campsite.
// TODO: Connect editing, notifications, rating and lottery data.
@Composable
fun OwnerCampsiteDetailsScreen(
    navController: NavController,
    campsite: Campsite?,
    onEditCampsite: (Campsite) -> Unit,
    onNotificationsClick: () -> Unit = {}
) {
    OwnerPagesLayout(
        navController = navController,
        onNotificationsClick = onNotificationsClick
    ) { innerPadding ->
        if (campsite == null) {
            Text(
                text = "Select a campsite first.",
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            )
        } else {
            var photoIndex by remember(campsite.documentId) {
                mutableIntStateOf(0)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = campsite.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(campsite.location)
                    }

                    TextButton(
                        onClick = { onEditCampsite(campsite) }
                    ) {
                        Text("Update details")
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(Color(0xFFF3DEDE)),
                    contentAlignment = Alignment.Center
                ) {
                    val photo = campsite.images.getOrNull(photoIndex)

                    if (photo == null) {
                        Image(
                            painter = painterResource(
                                R.drawable.missing_image_filler
                            ),
                            contentDescription = "No campsite photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        AsyncImage(
                            model = photo,
                            contentDescription = "Campsite photo ${photoIndex + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    if (campsite.images.size > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = {
                                    photoIndex = (
                                            photoIndex - 1 + campsite.images.size
                                            ) % campsite.images.size
                                }
                            ) {
                                Text("Previous")
                            }

                            TextButton(
                                onClick = {
                                    photoIndex = (
                                            photoIndex + 1
                                            ) % campsite.images.size
                                }
                            ) {
                                Text("Next")
                            }
                        }
                    }
                }

                Text(
                    text = "Description",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    campsite.description.ifBlank {
                        "No description yet."
                    }
                )

                Text(
                    text = "Tags",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                if (campsite.tags.isEmpty()) {
                    Text("No tags yet.")
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        campsite.tags.forEach { tag ->
                            Text("#$tag")
                        }
                    }
                }

                OwnerDetailRow(
                    "Maximum occupancy",
                    campsite.maxOcc.toString()
                )
                OwnerDetailRow("Current occupants", "—")
                OwnerDetailRow("Current waitlist count", "—")
                OwnerDetailRow("Next draw", "—")
                OwnerDetailRow("Campsite rating", "—")
            }
        }
    }
}

@Composable
private fun OwnerDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3DEDE))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium
        )
        Text(value)
    }
}

// Sample data is only used by Android Studio Preview.
@Preview(
    name = "Owner campsite details - sample data",
    showBackground = true,
    widthDp = 411,
    heightDp = 800
)
@Composable
private fun OwnerCampsiteDetailsPreview() {
    val campsite = remember {
        Campsite(
            "sample-1",
            "Dolor Campsite",
            "Edmonton",
            "A quiet campsite near walking trails. " +
                    "Picnic tables and drinking water are available.",
            9,
            mutableListOf(
                "Walking trails",
                "Picnic tables",
                "Drinking water"
            )
        )
    }

    OwnerCampsiteDetailsScreen(
        navController = rememberNavController(),
        campsite = campsite,
        onEditCampsite = {}
    )
}

// End of OwnerCampsiteDetailsScreen.kt