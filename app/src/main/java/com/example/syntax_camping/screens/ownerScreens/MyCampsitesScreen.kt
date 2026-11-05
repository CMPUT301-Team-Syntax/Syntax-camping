package com.example.syntax_camping.screens.ownerScreens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.TopBar
import com.example.syntax_camping.screens.CustomBottomBar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** US 02.03.01: renders the supplied database campsites in the owner layout. */
@Composable
fun MyCampsitesScreen(
    navController: NavController,
    campsites: List<Campsite>,
    onCampsiteClick: (Campsite) -> Unit,
    onAddCampsite: (() -> Unit)? = null,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null
) {
    OwnerPagesLayout(navController) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (onAddCampsite != null) {
                item {
                    Button(
                        onClick = onAddCampsite,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68))
                    ) {
                        Text("Add campsite")
                    }
                }
            }
            if (!isLoading && errorMessage == null && onRetry != null) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onRetry) { Text("Refresh") }
                    }
                }
            }
            when {
                isLoading -> item { OwnerDataMessage("Loading campsites…", loading = true) }
                errorMessage != null -> item { OwnerDataMessage(errorMessage, onRetry = onRetry) }
                campsites.isEmpty() -> item { Text("No campsites available.") }
                else -> items(campsites, key = { it.documentId }) { campsite ->
                    OwnerCampsiteCard(campsite) { onCampsiteClick(campsite) }
                }
            }
        }
    }
}

@Composable
internal fun OwnerDataMessage(
    message: String,
    loading: Boolean = false,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (loading) CircularProgressIndicator()
        Text(message, textAlign = TextAlign.Center)
        if (!loading && onRetry != null) {
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}

// Kept local so the owner's storyboard does not change other teams' screens.
@Composable
private fun OwnerCampsiteCard(campsite: Campsite, onClick: () -> Unit) {
    val placeholder = painterResource(R.drawable.missing_image_filler)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(4.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                val photo = campsite.images.firstOrNull { it.toString().isNotBlank() }
                if (photo == null) {
                    Image(
                        painter = placeholder,
                        contentDescription = "No photo for ${campsite.name}",
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    AsyncImage(
                        model = photo,
                        contentDescription = "Photo of ${campsite.name}",
                        placeholder = placeholder,
                        error = placeholder,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    campsite.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.location_on),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.DarkGray
                    )
                    Text(
                        campsite.location,
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // TODO: Replace the dash with the real lottery status at integration.
                Text(
                    "Lottery status: —",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OwnerCardStars(campsite.rating)
                    Text("${campsite.rating}/5", fontSize = 11.sp, color = Color.DarkGray)
                }
            }
        }
    }
}

/** Yellow rating stars shared by Zelin's owner list and details screens. */
@Composable
internal fun OwnerCardStars(
    rating: Double,
    modifier: Modifier = Modifier.size(width = 90.dp, height = 18.dp)
) {
    val value = if (rating.isFinite()) rating.coerceIn(0.0, 5.0) else 0.0
    val gold = Color(0xFFE2BC13)
    Canvas(
        modifier = modifier.semantics { contentDescription = "$value out of 5 stars" }
    ) {
        val slot = size.width / 5f
        val radius = minOf(slot, size.height) * 0.45f
        repeat(5) { star ->
            val centerX = slot * (star + 0.5f)
            val centerY = size.height / 2f
            val path = Path().apply {
                repeat(10) { point ->
                    val angle = -PI / 2 + point * PI / 5
                    val r = if (point % 2 == 0) radius else radius * 0.45f
                    val x = centerX + (cos(angle) * r).toFloat()
                    val y = centerY + (sin(angle) * r).toFloat()
                    if (point == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(path, gold, style = Stroke(width = 1.dp.toPx()))
            val fraction = (value - star).coerceIn(0.0, 1.0).toFloat()
            if (fraction > 0f) {
                clipRect(
                    left = centerX - radius,
                    right = centerX - radius + 2f * radius * fraction
                ) {
                    drawPath(path, gold)
                }
            }
        }
    }
}

/** Uses the same header and navigation bar as the team's other owner pages. */
@Composable
internal fun OwnerPagesLayout(
    navController: NavController,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        contentColor = Color.Black,
        topBar = { TopBar(navController) },
        bottomBar = {
            CustomBottomBar(navController, currentRoute = "CampsiteOwnerHome")
        },
        content = content
    )
}

// Sample data is only used by Android Studio Preview.
@Preview(name = "My Campsites - sample data", showBackground = true, widthDp = 411, heightDp = 800)
@Composable
private fun MyCampsitesPreview() {
    val campsites = remember {
        listOf(
            Campsite("sample-1", "Dolor Campsite", "Edmonton",
                "A quiet campsite near walking trails.", 9, mutableListOf("Walking trails"), rating = 4.4),
            Campsite("sample-2", "Lorem Campsite", "Calgary",
                "A small campsite with picnic tables.", 4, mutableListOf("Picnic tables"), rating = 4.2),
            Campsite("sample-3", "Ipsum Campsite", "Banff",
                "A campsite with mountain views.", 12, mutableListOf("Mountain views"), rating = 4.6)
        )
    }
    MyCampsitesScreen(
        navController = rememberNavController(),
        campsites = campsites,
        onCampsiteClick = {}
    )
}

@Preview(name = "My Campsites - empty", showBackground = true, widthDp = 411, heightDp = 800)
@Composable
private fun EmptyMyCampsitesPreview() {
    MyCampsitesScreen(
        navController = rememberNavController(),
        campsites = emptyList(),
        onCampsiteClick = {}
    )
}