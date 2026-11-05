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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

/** US 02.01.04: renders the selected database campsite in one scrollable page. */
@Composable
fun OwnerCampsiteDetailsScreen(
    navController: NavController,
    campsite: Campsite?,
    onEditCampsite: (Campsite) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null
) {
    val pink = Color(0xFFEBC3C3)
    OwnerPagesLayout(navController) { innerPadding ->
        if (isLoading || errorMessage != null || campsite == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                OwnerDataMessage(
                    message = when {
                        isLoading -> "Loading campsite…"
                        errorMessage != null -> errorMessage
                        else -> "This campsite is no longer available."
                    },
                    loading = isLoading,
                    onRetry = onRetry
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { onEditCampsite(campsite) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3E6B68),
                        contentColor = Color.White
                    )
                ) {
                    Text("Update Campsite")
                }
                if (onRetry != null) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onRetry) { Text("Refresh") }
                    }
                }
                OwnerPhotoSection(campsite)
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .heightIn(min = 280.dp)
                        .background(pink, RoundedCornerShape(10.dp)).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(campsite.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(campsite.location, fontSize = 14.sp)
                    Text("Campsite description", fontWeight = FontWeight.Bold)
                    Text(campsite.description.ifBlank { "No description yet." })
                }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(pink, RoundedCornerShape(10.dp))
                            .horizontalScroll(rememberScrollState()).padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val tags = campsite.tags.filter { it.isNotBlank() }
                        if (tags.isEmpty()) {
                            Text("No tags yet.")
                        } else {
                            tags.forEach { tag -> Text("#$tag") }
                        }
                    }
                    OwnerDetailRow("Maximum occupants", campsite.maxOcc.toString())
                    // TODO: Bind these three values when lottery data is integrated.
                    OwnerDetailRow("Current occupants", "—")
                    OwnerDetailRow("Current waitlist count", "—")
                    OwnerDetailRow("Next draw", "—")
                    Spacer(Modifier.height(32.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(pink, RoundedCornerShape(10.dp)).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Campsite Rating:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        OwnerCardStars(
                            rating = campsite.rating,
                            modifier = Modifier.size(width = 140.dp, height = 28.dp)
                        )
                        Text("${campsite.rating}/5", fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun OwnerPhotoSection(campsite: Campsite) {
    val photos = campsite.images.filter { it.toString().isNotBlank() }
    // Reset when the number of photos changes, keeping the selected index valid.
    var photoIndex by remember(campsite.documentId, photos) { mutableIntStateOf(0) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .background(Color(0xFFE6E6E6), RoundedCornerShape(10.dp))
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            val photo = photos.getOrNull(photoIndex)
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
                    placeholder = painterResource(R.drawable.missing_image_filler),
                    error = painterResource(R.drawable.missing_image_filler),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        if (photos.size > 1) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = {
                    photoIndex = (photoIndex - 1 + photos.size) % photos.size
                }) { Text("Previous") }
                TextButton(onClick = {
                    photoIndex = (photoIndex + 1) % photos.size
                }) { Text("Next") }
            }
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(photos.size.coerceAtLeast(1)) { index ->
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
