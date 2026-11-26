package com.example.syntax_camping.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.syntax_camping.Campsite

/** Side picture on every campsite card: first image if there is one, orange block if not. */
@Composable
fun CampsiteThumbnail(campsite: Campsite, modifier: Modifier = Modifier) {
    val firstImage = campsite.images.firstOrNull()?.toString()?.takeIf { it.isNotBlank() }

    val shapeModifier = modifier
        .width(96.dp)
        .height(96.dp)
        .clip(RoundedCornerShape(16.dp))

    if (firstImage != null) {
        AsyncImage(
            model = firstImage,
            contentDescription = "${campsite.name} picture",
            contentScale = ContentScale.Crop,
            modifier = shapeModifier.testTag("campsiteThumbnail_${campsite.documentId}")
        )
    } else {
        Box(
            modifier = shapeModifier
                .background(Color(0xFFF4E8D5))
                .testTag("campsiteThumbnailEmpty_${campsite.documentId}")
        )
    }
}