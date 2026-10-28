package com.example.syntax_camping.screens.ownerScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.syntax_camping.screens.Rating

// US 02.01.04: owner details layout from the team's owner storyboard.
// TODO: Connect notifications, editing and the remaining lottery information.
@Composable
fun OwnerCampsiteDetailsScreen(
    navController: NavController,
    campsite: Campsite?,
    onEditCampsite: (Campsite) -> Unit,
    onNotificationsClick: () -> Unit = {}
) {
    val pink = Color(0xFFEBC3C3)

    OwnerPagesLayout(
        navController = navController,
        onNotificationsClick = onNotificationsClick,
        onEditCampsite = campsite?.let { selected ->
            { onEditCampsite(selected) }
        }
    ) { innerPadding ->
        if (campsite == null) {
            Text(
                "Select a campsite first.",
                Modifier.padding(innerPadding).padding(16.dp)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OwnerPhotoSection(campsite)

                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 280.dp)
                        .background(pink).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(campsite.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(campsite.location, fontSize = 14.sp)
                    Text("Campsite description", fontWeight = FontWeight.Bold)
                    Text(campsite.description.ifBlank { "No description yet." })
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(pink, RoundedCornerShape(10.dp))
                            .horizontalScroll(rememberScrollState()).padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (campsite.tags.isEmpty()) {
                            Text("No tags yet.")
                        } else {
                            campsite.tags.forEach { tag -> Text("#$tag") }
                        }
                    }

                    OwnerDetailRow("Maximum occupants", campsite.maxOcc.toString())
                    OwnerDetailRow("Current occupants", "—")
                    OwnerDetailRow("Current waitlist count", "—")
                    OwnerDetailRow("Next draw", "—")

                    Spacer(Modifier.height(64.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(pink, RoundedCornerShape(10.dp)).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Campsite Rating:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Rating(numRating = campsite.rating)
                        Text("${campsite.rating}/5", fontSize = 14.sp)
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun OwnerPhotoSection(campsite: Campsite) {
    var photoIndex by remember(campsite.documentId) { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxWidth()
            .background(Color(0xFFE6E6E6)).padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            val photo = campsite.images.getOrNull(photoIndex)
            if (photo == null) {
                Image(
                    painter = painterResource(R.drawable.missing_image_filler),
                    contentDescription = "No campsite photo",
                    modifier = Modifier.fillMaxSize().padding(24.dp),
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
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        photoIndex = (photoIndex - 1 + campsite.images.size) % campsite.images.size
                    }) { Text("Previous") }
                    TextButton(onClick = {
                        photoIndex = (photoIndex + 1) % campsite.images.size
                    }) { Text("Next") }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(campsite.images.size.coerceAtLeast(1)) { index ->
                Box(
                    Modifier.size(6.dp).background(
                        if (index == photoIndex) Color.DarkGray else Color.LightGray,
                        CircleShape
                    )
                )
            }
        }
    }
}

@Composable
private fun OwnerDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Color(0xFFEBC3C3), RoundedCornerShape(10.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text(value)
    }
}

// Only these previews use sample data.
@Preview(name = "Owner details - phone", showBackground = true, widthDp = 411, heightDp = 800)
@Preview(name = "Owner details - full layout", showBackground = true, widthDp = 411, heightDp = 1400)
@Composable
private fun OwnerCampsiteDetailsPreview() {
    val campsite = remember {
        Campsite(
            documentId = "sample-1",
            name = "Dolor Campsite",
            location = "Edmonton",
            description = "A quiet campsite near walking trails. Picnic tables and drinking water are available.",
            maxOcc = 9,
            tags = mutableListOf("Walking trails", "Picnic tables", "Drinking water"),
            rating = 4.4
        )
    }

    OwnerCampsiteDetailsScreen(
        navController = rememberNavController(),
        campsite = campsite,
        onEditCampsite = {}
    )
}

// End of OwnerCampsiteDetailsScreen.kt
